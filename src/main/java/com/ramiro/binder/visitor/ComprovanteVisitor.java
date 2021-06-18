package com.ramiro.binder.visitor;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.apache.commons.text.WordUtils;

import com.google.gson.GsonBuilder;

import com.jayway.jsonpath.JsonPath;

import com.ramiro.binder.generated.ComprovBaseVisitor;
import com.ramiro.binder.generated.ComprovParser;
import com.ramiro.binder.generated.ComprovParser.AbbreviateContext;
import com.ramiro.binder.generated.ComprovParser.AndExprContext;
import com.ramiro.binder.generated.ComprovParser.BooleanoContext;
import com.ramiro.binder.generated.ComprovParser.CapitalizeContext;
import com.ramiro.binder.generated.ComprovParser.CnpjContext;
import com.ramiro.binder.generated.ComprovParser.ColchetesContext;
import com.ramiro.binder.generated.ComprovParser.ComparativoContext;
import com.ramiro.binder.generated.ComprovParser.ConcatenarContext;
import com.ramiro.binder.generated.ComprovParser.ContainsContext;
import com.ramiro.binder.generated.ComprovParser.CpfContext;
import com.ramiro.binder.generated.ComprovParser.DateContext;
import com.ramiro.binder.generated.ComprovParser.FormatCurrencyContext;
import com.ramiro.binder.generated.ComprovParser.IfContext;
import com.ramiro.binder.generated.ComprovParser.InitialsContext;
import com.ramiro.binder.generated.ComprovParser.IsCnpjContext;
import com.ramiro.binder.generated.ComprovParser.IsCpfContext;
import com.ramiro.binder.generated.ComprovParser.JsonContext;
import com.ramiro.binder.generated.ComprovParser.NumeroContext;
import com.ramiro.binder.generated.ComprovParser.OrExprContext;
import com.ramiro.binder.generated.ComprovParser.RelacionalContext;
import com.ramiro.binder.generated.ComprovParser.StringContext;
import com.ramiro.binder.generated.ComprovParser.ToLowerCaseContext;
import com.ramiro.binder.generated.ComprovParser.ToNumberContext;
import com.ramiro.binder.generated.ComprovParser.ToUpperCaseContext;
import com.ramiro.binder.generated.ComprovParser.UncapitalizeContext;
import com.ramiro.binder.visitor.model.Value;

import br.com.caelum.stella.tinytype.CNPJ;
import br.com.caelum.stella.tinytype.CPF;

public class ComprovanteVisitor extends ComprovBaseVisitor<Value> {

	private final String json;

	public ComprovanteVisitor(Object object) {

		this.json = new GsonBuilder().create().toJson(object);
	}

	private Locale tratarLinguagemEPais(String linguagemEPais) {

		String msgErro = "linguagem e pais deve ter o padrao \"pt-br\".";

		if (!linguagemEPais.contains("-"))
			throw new ParseCancellationException(msgErro);

		String[] lang = linguagemEPais.split("-");

		if (lang.length != 2)
			throw new ParseCancellationException(msgErro);

		return new Locale(lang[0], lang[1]);

	}

	private String obterValorJson(String padrao) {

		if (!padrao.contains("$"))
			return padrao;

		try {

			Object obj = JsonPath.read(this.json, padrao);

			if (obj instanceof List<?>)
				return ((List<?>) obj).stream().findFirst().isPresent()
						? ((List<?>) obj).stream().findFirst().get().toString()
						: "";

			return obj == null ? "" : obj.toString();

		} catch (Exception e) {
			//e.printStackTrace();
			throw new ParseCancellationException("Erro ao chamar JsonPath. Padrao recebido: " +  padrao + ".", e);
		}
	}

	@Override
	public Value visitJson(JsonContext ctx) {
		return new Value(this.obterValorJson(this.visit(ctx.expressao()).asString()));
	}

	@Override
	public Value visitConcatenar(ConcatenarContext ctx) {
		Value left = this.visit(ctx.expressao(0));
		Value right = this.visit(ctx.expressao(1));
		if (left.isDecimal() && right.isDecimal())
			return new Value(left.asDecimal().add(right.asDecimal()));
		return new Value(left.asString() + right.asString());

	}

	@Override
	public Value visitNumero(NumeroContext ctx) {
		return new Value(new BigDecimal(ctx.getText()));
	}

	@Override
	public Value visitString(StringContext ctx) {
		String str = ctx.getText();
		str = str.substring(1, str.length() - 1).replace("\"\"", "\"");
		return new Value(str);
	}

	@Override
	public Value visitBooleano(BooleanoContext ctx) {
		return new Value(Boolean.valueOf(ctx.getText()));
	}

	@Override
	public Value visitColchetes(ColchetesContext ctx) {
		return this.visit(ctx.expressao());
	}

	@Override
	public Value visitComparativo(ComparativoContext ctx) {
		Value left = this.visit(ctx.expressao(0));
		Value right = this.visit(ctx.expressao(1));

		switch (ctx.op.getType()) {
		case ComprovParser.IGUAL:
			return left.isDecimal() && right.isDecimal()
					? new Value(Boolean.valueOf(left.asDecimal().compareTo(right.asDecimal()) == 0))
					: new Value(Boolean.valueOf(left.asString().equals(right.asString())));

		case ComprovParser.DIFERENTE:
			return left.isDecimal() && right.isDecimal()
					? new Value(Boolean.valueOf(left.asDecimal().compareTo(right.asDecimal()) != 0))
					: new Value(Boolean.valueOf(!left.equals(right)));

		default:
			throw new ParseCancellationException("operador desconhecido: " + ctx.op.getText());
		}
	}

	@Override
	public Value visitRelacional(RelacionalContext ctx) {
		Value left = this.visit(ctx.expressao(0));
		Value right = this.visit(ctx.expressao(1));

		if (!left.isDecimal() || !right.isDecimal())
			return Value.VOID;

		switch (ctx.op.getType()) {
		case ComprovParser.MAIOR:
			return new Value(Boolean.valueOf(left.asDecimal().compareTo(right.asDecimal()) == 1));

		case ComprovParser.MAIORIGUAL:
			return new Value(Boolean.valueOf(left.asDecimal().compareTo(right.asDecimal()) >= 0));

		case ComprovParser.MENOR:
			return new Value(Boolean.valueOf(left.asDecimal().compareTo(right.asDecimal()) == -1));

		case ComprovParser.MENORIGUAL:
			return new Value(Boolean.valueOf(left.asDecimal().compareTo(right.asDecimal()) <= 0));

		default:
			throw new ParseCancellationException("operador desconhecido: " + ctx.op.getText());
		}

	}

	@Override
	public Value visitAndExpr(AndExprContext ctx) {
		if (!this.visit(ctx.expressao(0)).asBoolean())
			return new Value(Boolean.FALSE);

		if (!this.visit(ctx.expressao(1)).asBoolean())
			return new Value(Boolean.FALSE);

		return new Value(Boolean.TRUE);
	}

	@Override
	public Value visitOrExpr(OrExprContext ctx) {

		if (this.visit(ctx.expressao(0)).asBoolean())
			return new Value(Boolean.TRUE);

		if (this.visit(ctx.expressao(1)).asBoolean())
			return new Value(Boolean.TRUE);

		return new Value(Boolean.FALSE);
	}

	@Override
	public Value visitCapitalize(CapitalizeContext ctx) {
		return new Value(WordUtils.capitalize(this.visit(ctx.expressao()).asString()));
	}

	@Override
	public Value visitUncapitalize(UncapitalizeContext ctx) {
		return new Value(WordUtils.uncapitalize(this.visit(ctx.expressao()).asString()));
	}

	@Override
	public Value visitToUpperCase(ToUpperCaseContext ctx) {
		return new Value(this.visit(ctx.expressao()).asString().toUpperCase());
	}

	@Override
	public Value visitToLowerCase(ToLowerCaseContext ctx) {
		return new Value(this.visit(ctx.expressao()).asString().toLowerCase());
	}

	@Override
	public Value visitInitials(InitialsContext ctx) {
		char[] chars = { ' ', '.' };
		return new Value(WordUtils.initials(this.visit(ctx.expressao()).asString(), chars));
	}

	@Override
	public Value visitAbbreviate(AbbreviateContext ctx) {

		String texto = this.visit(ctx.str).asString();
		int lower = this.visit(ctx.lower).asDecimal().intValue();
		int upper = this.visit(ctx.upper).asDecimal().intValue();

		return new Value(WordUtils.abbreviate(texto, lower, upper, ""));
	}

	@Override
	public Value visitContains(ContainsContext ctx) {

		String texto = this.visit(ctx.str).asString();
		String sequence = this.visit(ctx.sequence).asString();

		return new Value(Boolean.valueOf(texto.toLowerCase().contains(sequence.toLowerCase())));

	}

	@Override
	public Value visitFormatCurrency(FormatCurrencyContext ctx) {

		Value value = this.visit(ctx.value);
		if (!value.isDecimal())
			throw new ParseCancellationException("valor deve ser numero/decimal :: " + value.asString());

		Locale locale = tratarLinguagemEPais(this.visit(ctx.lang_country).asString());

		NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);

		return new Value(currencyFormatter.format(value.asDecimal()));

	}

	@Override
	public Value visitIf(IfContext ctx) {

		Value resultado = this.visit(ctx.teste);

		if (!resultado.isBoolean())
			throw new ParseCancellationException("expressao deve retornar um boolean :: " + resultado.asString());

		if (resultado.asBoolean().booleanValue()) {
			return this.visit(ctx.verdadeiro);
		}
		return this.visit(ctx.falso);
	}

	@Override
	public Value visitCpf(CpfContext ctx) {

		Value value = this.visit(ctx.expressao());
		String tratado = value.asString().replaceAll("\\s+", "");
		return new Value(new CPF(tratado).getNumeroFormatado());

	}

	@Override
	public Value visitDate(DateContext ctx) {

		Value value = this.visit(ctx.value);

		if (!value.isString())
			throw new ParseCancellationException("valor deve ser string :: " + value.asString());

		String masc_entrada = this.visit(ctx.masc_e).asString();
		String masc_saida = this.visit(ctx.masc_s).asString();
		Locale locale = tratarLinguagemEPais(this.visit(ctx.lang_country).asString());
		SimpleDateFormat formatadorEntrada = new SimpleDateFormat(masc_entrada, locale);
		SimpleDateFormat formatadorSaida = new SimpleDateFormat(masc_saida, locale);

		try {
			return new Value(formatadorSaida.format(formatadorEntrada.parse(value.asString())));

		} catch (ParseException e) {
			throw new ParseCancellationException("nao foi possivel converter para data :: " + value.asString(), e);
		}

	}

	@Override
	public Value visitIsCpf(IsCpfContext ctx) {
		Value value = this.visit(ctx.expressao());
		String tratado = value.asString().replaceAll("\\s+", "");
		return new Value(Boolean.valueOf(new CPF(tratado).isValido()));

	}

	@Override
	public Value visitCnpj(CnpjContext ctx) {
		Value value = this.visit(ctx.expressao());
		String tratado = value.asString().replaceAll("\\s+", "");
		return new Value(new CNPJ(tratado).getNumeroFormatado());
	}

	@Override
	public Value visitIsCnpj(IsCnpjContext ctx) {
		Value value = this.visit(ctx.expressao());
		String tratado = value.asString().replaceAll("\\s+", "");
		return new Value(Boolean.valueOf(new CNPJ(tratado).isValid()));
	}

	@Override
	public Value visitToNumber(ToNumberContext ctx) {

		Value value = this.visit(ctx.expressao());

		if (value.isDecimal())
			return value;

		String tratado = value.asString().replaceAll("\\s+", "");

		try {
			BigDecimal number = new BigDecimal(tratado);
			return new Value(number);
		} catch (NumberFormatException e) {
			throw new ParseCancellationException("Não é possível converter o valor para Numero :: " + value.asString(), e);
		}

	}

}