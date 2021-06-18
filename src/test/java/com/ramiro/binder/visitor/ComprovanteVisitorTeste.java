package com.ramiro.binder.visitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

import com.ramiro.binder.generated.ComprovLexer;
import com.ramiro.binder.generated.ComprovParser;
import com.ramiro.binder.visitor.model.Value;

import br.com.caelum.stella.tinytype.CPF;

public class ComprovanteVisitorTeste {
	
	private static Cliente getCliente() {
		
		Cliente cliente = new Cliente();
		cliente.setNome("MARCOS");
		cliente.setIdade(31);
		cliente.setCpf("70643401008");
		cliente.setSalario(new BigDecimal("2000.50"));
		
		Cliente cliente2 = new Cliente();
		cliente2.setNome("jose");
		cliente2.setIdade(45);
		cliente2.setCpf("123");
		cliente.setCliente(cliente2);
		
		Cliente cliente3 = new Cliente();
		cliente3.setNome("joao");
		cliente3.setIdade(52);
		cliente3.setCpf("456");
		
		List<Cliente> clientes = new ArrayList<>();
		
		clientes.add(cliente2);
		clientes.add(cliente3);
		cliente.setClientes(clientes);
		return cliente;
	}
	
	private static Value chamarVisitor(String padrao, Object object) {
		
		ComprovLexer lexer = new ComprovLexer(CharStreams.fromString(padrao));
		ComprovanteVisitorErrorListener error = new ComprovanteVisitorErrorListener();
		lexer.removeErrorListeners();
		lexer.addErrorListener(error);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ComprovParser parser = new ComprovParser(tokens);
		parser.removeErrorListeners();
		parser.addErrorListener(error);
		ParseTree tree = parser.programa();
		ComprovanteVisitor visitor = new ComprovanteVisitor(object);
		return visitor.visit(tree);
	}

	@Test
	public void deveCompararDoisInteriosIguais() {
		
		String padrao = " 22 == 22 ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("true", value.asString());
		assertTrue(value.asBoolean());

	}
	
	@Test
	public void deveCompararDoisInteriosIguaisResultadoDaFuncaotoNumber() {
		
		String padrao = " tonumber(json(\"$.salario\")) == 2000.50 ";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("true", value.asString());
		assertTrue(value.asBoolean());

	}
	
	@Test
	public void deveconverterNumeroParaNumero_toNumber() {
		
		String padrao = " tonumber( 2000.50 ) ";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("2000.50", value.asString());
		assertTrue(value.isDecimal());

	}
	
	

	@Test
	public void deveCompararESomarJuntosComColchetes() {

		String padrao = " 23 > [11 + 0005]";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("true", value.asString());
		assertTrue(value.asBoolean());

	}

	@Test
	public void deveCompararDoisInteriosDiferente_Igual() {
		
		String padrao = " 22 == 21 ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("false", value.asString());
		assertFalse(value.asBoolean());


	}

	@Test
	public void deveCompararDoisInteriosIguais_Diferente() {

		String padrao = " 21 != 21 ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("false", value.asString());
		assertFalse(value.asBoolean());

	}

	@Test
	public void deveCompararDoisInteriosDiferentes_Diferente() {

		String padrao = " 22 != 21 ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("true", value.asString());
		assertTrue(value.asBoolean());

	}

	@Test
	public void deveCompararDuasStringsIguais_Igual() {

		String padrao = " \"aqui é um teste\" == \"aqui é um teste\" ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("true", value.asString());
		assertTrue(value.asBoolean());

	}

	@Test
	public void deveCompararDuasStringsDiferentes_Igual() {

		String padrao = " \"aqui é um teste\" == \"texto diferente\" ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("false", value.asString());
		assertFalse(value.asBoolean());

	}

	@Test
	public void deveCompararDuasStringsDiferentes_Diferente() {

		String padrao = " \"aqui é um teste\" != \"texto diferente\" ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("true", value.asString());
		assertTrue(value.asBoolean());

	}

	@Test
	public void deveCompararDuasStringsIguais_Diferente() {

		String padrao = " \"aqui é um teste\" != \"aqui é um teste\" ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("false", value.asString());
		assertFalse(value.asBoolean());

	}

	@Test
	public void deveCompararNumeroMenor_Menor() {

		String padrao = " 1 < 2 ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("true", value.asString());
		assertTrue(value.asBoolean());

	}

	@Test
	public void deveCompararNumeroMaior_Menor() {

		String padrao = " 2 < 1 ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("false", value.asString());
		assertFalse(value.asBoolean());

	}

	@Test
	public void deveCompararNumeroMaior_MaiorIgual() {

		String padrao = " 2 >= 1 ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("true", value.asString());
		assertTrue(value.asBoolean());

	}

	@Test
	public void deveCompararNumeroIgual_MaiorIgual() {

		String padrao = " 2 >= 2 ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("true", value.asString());
		assertTrue(value.asBoolean());

	}

	@Test
	public void deveCompararNumeroMenor_MaiorIgual() {

		String padrao = " 1 >= 2 ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("false", value.asString());
		assertFalse(value.asBoolean());
	}

	@Test
	public void deveCompararNumeroMenor_MenorIgual() {

		String padrao = " 1 <= 2 ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("true", value.asString());
		assertTrue(value.asBoolean());

	}

	@Test
	public void deveCompararNumerosguais_MenorIgual() {

		String padrao = " 2 <= 2 ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("true", value.asString());
		assertTrue(value.asBoolean());

	}

	@Test
	public void deveCompararNumeroMaior_MenorIgual() {

		String padrao = " 3 <= 2 ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("false", value.asString());
		assertFalse(value.asBoolean());

	}

	@Test
	public void deveCompararDuasAfirmacoesSaoVerdadeiras_And() {

		String padrao = " 2 == 2 && 9 > 5 ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("true", value.asString());
		assertTrue(value.asBoolean());

	}
		
	@Test
	public void deveCompararDuasAfirmacoesSendoASegundaFalsa_And() {

		String padrao = " 2 == 2 && 9 < 5 ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("false", value.asString());
		assertFalse(value.asBoolean());

	}

	@Test
	public void deveCompararApenasUmaAfirmacaoEhVerdadeira_And() {

		String padrao = " 2 != 2 && 9 > 5 ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("false", value.asString());
		assertFalse(value.asBoolean());

	}

	@Test
	public void deveCompararQuandoAsDuasfirmacaoSaoFalsas_And() {

		String padrao = " 2 != 2 && 9 < 5 ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("false", value.asString());
		assertFalse(value.asBoolean());

	}

	@Test
	public void deveCompararQuandoUmaAfirmacaoEhVerdadeira_Or() {

		String padrao = " 2 > 2 || 9 == 9 ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("true", value.asString());
		assertTrue(value.asBoolean());

	}

	@Test
	public void deveCompararQuandoAsDuasAfirmacaoSaoVerdadeiras_Or() {

		String padrao = " 2 == 2 || 9 == 9 ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("true", value.asString());
		assertTrue(value.asBoolean());

	}

	@Test
	public void deveCompararQuandoAsDuasAfirmacaoSaoFalsas_Or() {

		String padrao = " [2 == 1 || 8 == 9] ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("false", value.asString());
		assertFalse(value.asBoolean());

	}

	@Test
	public void deveConcatenarDuasStrings_Concatenar() {

		String padrao = " \"Meu nome é \" + \"João\" ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("Meu nome é João", value.asString());

	}

	@Test
	public void deveConcatenarStringENumero_Concatenar() {

		String padrao = " \"O valor total foi \" + 2.2 ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("O valor total foi 2.2", value.asString());
		
		padrao = " 2.2 + \" foi o valor total\"";
		value = chamarVisitor(padrao, new Object());
		assertEquals("2.2 foi o valor total", value.asString());

	}

	@Test
	public void deveSomarDoisNumero_Concatenar() {
		
		String padrao = " 4.78 + 2.2 ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("6.98", value.asString());
		assertTrue(new BigDecimal("6.98").equals(value.asDecimal()));

	}

	@Test
	public void deveRetornarUmNumero() {

		String padrao = " 4.78 ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("4.78", value.asString());
		assertTrue(new BigDecimal("4.78").equals(value.asDecimal()));

	}

	@Test
	public void deveRetornarUmaString() {
	
		String padrao = " \"Meu nome é João da Silva\" ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("Meu nome é João da Silva", value.asString());

	}

	@Test
	public void deveRetornarUmBooleanTrue() {

		String padrao = " true ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("true", value.asString());
		assertTrue(value.asBoolean());

	}

	@Test
	public void deveRetornarUmBooleanFalse() {

		String padrao = " false ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("false", value.asString());
		assertFalse(value.asBoolean());

	}

	@Test
	public void deveTratarParenteses_Boolean() {

		String padrao = " [ 1 > 2 || 45 != 45 ]  == true ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("false", value.asString());
		assertFalse(value.asBoolean());

	}
	
	@Test
	public void deveTratarColchetesVerdadeiro_Boolean() {

		String padrao = " [ 12 > 2 && 45 == 45 ]  == true ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("true", value.asString());
		assertTrue(value.asBoolean());

	}

	@Test
	public void deveTratarColchetes_Numero() {

		String padrao = " [ 4 + 5  ]  == 9 ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("true", value.asString());
		assertTrue(value.asBoolean());

	}

	@Test
	public void deveTratarColchetes_somaNumeros() {

		String padrao = " [ 4 + 5  ]  + 2 ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("11", value.asString());

	}

	@Test
	public void deveRetornarPrimeirasLetrasEmMaiuscula_Capitalize() {

		String padrao = " capitalize ( \"MEU nome é joãozinho\" ) ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("MEU Nome É Joãozinho", value.asString());

	}
	
	@Test
	public void deveRetornarPrimeirasLetrasEmMinuscula_Uncapitalize() {

		String padrao = " uncapitalize ( \"MEU nOmE NÃO É jOHnnY\" ) ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("mEU nOmE nÃO é jOHnnY", value.asString());

	}

	@Test
	public void deveRetornarConcatenarFuncaoCapitalizeEString_Concatenar() {

		String padrao = " capitalize ( \"MEU noMe é joãozinho\" ) + \"!!\" ";
		Value value = chamarVisitor(padrao, new Object());
		assertEquals("MEU NoMe É Joãozinho!!", value.asString());

	}
	
	@Test
	public void deveObterValorAPartirDeJsonPath_Json() {
		
		String padrao = " json ( \"$.cliente.nome\" ) ";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("jose", value.asString());

	}
	
	@Test
	public void deveObterPrimeiroValorDeUmaLista_Json() {
		
		String padrao = "json(\"$.clientes[?(@.idade > 2)].nome\")";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("jose", value.asString());

	}
	
	@Test
	public void deveRetornarVazioQuandoAcharListaVazia_Json() {
		
		String padrao = "json(\"$.clientes[?(@.idade > 99)].nome\")";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("", value.asString());

	}

	@Test
	public void deveCombinarFuncoesJsonECaptalize() {
		
		String padrao = " capitalize ( json ( \"$.cliente.nome\" )  ) ";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("Jose", value.asString());

	}
	
	

	@Test
	public void deveTratarIfVerdadeiro_If() {

		String padrao = " capitalize ( json ( \"$.cliente.nome\" )  ) == \"Jose\" ? \"verdadeiro\" : \"falso\" ";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("verdadeiro", value.asString());

	}

	@Test
	public void deveTratarIfFalso_If() {

		String padrao = " capitalize ( json ( \"$.cliente.nome\" )  ) == \"Maria\" ? \"verdadeiro\" : \"falso\" ";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("falso", value.asString());

	}
	
	@Test
	public void deveTratarStringsParaUpperCase_toUpperCase() {

		String padrao = " touppercase  ( \"mEU NOmE NÃO É jOHnnY\" ) ";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("MEU NOME NÃO É JOHNNY", value.asString());

	}
	
	@Test
	public void deveTratarStringsParaLowerCase_toLowerCase() {

		String padrao = " tolowercase  ( \"mEU NOmE NÃO É jOHnnY\" ) ";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("meu nome não é johnny", value.asString());

	}
	
	@Test
	public void deveTratarStringsParaLowerCaseECapitalize() {

		String padrao = " capitalize (  tolowercase  ( \"mEU NOmE NÃO É jOHnnY\") ) ";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("Meu Nome Não É Johnny", value.asString());

	}
	
	@Test
	public void deveTratarStringsParaLowerCaseECapitalizeEJson() {

		String padrao = " capitalize (  tolowercase  ( json ( \"$.nome\" ) ) ) + \" Ramiro\" ";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("Marcos Ramiro", value.asString());

	}
	
	@Test
	public void deveObterAsIniciaisDeUmaStringComPonto() {

		String padrao = " initials (  \"Ben J.Lee\" ) ";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("BJL", value.asString());

	}
	
	@Test
	public void deveObterAsIniciaisDeUmaStringSemPontoMaiusculas() {

		String padrao = " touppercase ( initials (  \"Maria da silva Santos\"  )) ";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("MDSS", value.asString());

	}
	
	@Test
	public void deveAbreviarTextoGrande() {

		String padrao = " abbreviate (  \"Maria da silva Santos\", 10, 20 ) ";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("Maria da silva", value.asString());

	}

	@Test
	public void deveGerarErro_FuncaoNaoExiste() {

		String padrao = " capitalize ( jsonX ( \"$.cliente.nome\" )) ";
		
		Exception exception = assertThrows(ParseCancellationException.class, () -> {
			Value value = chamarVisitor(padrao, getCliente());
		});
		
		assertTrue(exception.getMessage().contains("token recognition error"));
		
	}
	
	@Test
	public void deveGerarErroQuandoInformarPadraoComErroParaFuncaoJson() {

		String padrao = " json ( \"$.cliente.clientes[0].campoNaoExiste\" )) ";
		
		Exception exception = assertThrows(ParseCancellationException.class, () -> {
			Value value = chamarVisitor(padrao, getCliente());
		});
		
		assertEquals("Erro ao chamar JsonPath. Padrao recebido: $.cliente.clientes[0].campoNaoExiste.", exception.getMessage());		
		
	}
	
	@Test
	public void deveTratarFuncaoDentroDeUmaStringComoString() {

		String padrao = " \" capitalize ( json(200.00) ) \" ";
		
		Value value = chamarVisitor(padrao, getCliente());
		
		assertEquals(" capitalize ( json(200.00) ) ", value.asString());
		
	}

	@Test
	public void deveGerarErro_PalavraNaoReservadaForaDeExpressao() {
		
		String padrao = " capitalize ( json XPTO ( \"$.cliente.nome\" )) ";
		
		Exception exception = assertThrows(ParseCancellationException.class, () -> {
			Value value = chamarVisitor(padrao, getCliente());
		});
		
		assertTrue(exception.getMessage().contains("token recognition error"));

	}
	
	@Test
	public void deveValidarSeTextoContemConteudo() {
		
		String padrao = "contains(json( \"$.cliente.nome\"), \"e\") ";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("true", value.asString());
		assertTrue(value.asBoolean());

	}
	
	@Test
	public void deveValidarQueTextoNaoContemConteudo() {
		
		String padrao = "contains(json( \"$.cliente.nome\"), \"w\") ";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("false", value.asString());
		assertFalse(value.asBoolean());

	}
		
	@Test
	public void deveGerarErro_IfNaoTemBoolean() {
		
		String padrao = " \"Maria da Silva\" ? true : false ";
		
		Exception exception = assertThrows(ParseCancellationException.class, () -> {
			Value value = chamarVisitor(padrao, getCliente());
		});
		
		assertEquals("expressao deve retornar um boolean :: " + "Maria da Silva", exception.getMessage());

	}
	
	@Test
	public void deveFormatarParaRealBrasileiro() {
		String padrao = " formatcurrency ( 123.12 , \"pt-br\" ) ";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals( "R$ 123,12", value.asString());
		
	}
	
	@Test
	public void deveGerarErroAoTentarFormatarTextoParaMoeda() {
		
		String padrao = " formatcurrency ( \"Maria\" , \"pt-br\" ) ";

		Exception exception = assertThrows(ParseCancellationException.class, () -> {
			Value value = chamarVisitor(padrao, getCliente());
		});
		
		assertEquals("valor deve ser numero/decimal :: " + "Maria", exception.getMessage());

	}
	
	@Test
	public void deveFormatarCPFComPontuacao() {
		
		
		String padrao = " cpf (   \"  674.798.460-96   \" ) ";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("674.798.460-96", value.asString());
	}
	
	@Test
	public void deveFormatarCPFSemPontuacao() {
		
		String padrao = "cpf(\"  67479846096   \")";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("674.798.460-96", value.asString());
		
	}
	
	@Test
	public void DeveFormatarCPFInvalidoSemPontuacao() {
		
		String padrao = "cpf(\"  12345678912   \")";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("123.456.789-12", value.asString());
		
	}
	
	@Test
	public void naoDeveFormatarCPFInvalidoMaiorDoQueOnzeNumeroSemPontuacao() {
		
		String padrao = "cpf(\"  12345678912123   \")";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("12345678912123", value.asString());
		
	}
	
	@Test
	public void naoDeveFormatarCPFInvalidoMaiorDoQueOnzeNumeroComPontuacao() {
		
		String padrao = "cpf(\"  123.4567.9-12123   \")";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("123.4567.9-12123", value.asString());
		
	}
	
	@Test
	public void deveIdentificarCpfValidoSemPontuacao() {
		String padrao = "iscpf(\"  25439329099   \")";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("true", value.asString());
		assertTrue(value.asBoolean());
	}
	
	@Test
	public void deveIdentificarCpfValidoComPontuacao() {
		String padrao = "iscpf(\"  254.393.290-99   \")";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("true", value.asString());
		assertTrue(value.asBoolean());
	}
	
	@Test
	public void deveIdentificarCpfValidoComPontuacaoComJson() {
		String padrao = "iscpf(json( \"$.cpf\")) ? cpf(json(\"$.cpf\")) : false";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals(new CPF(getCliente().getCpf()).getNumeroFormatado(), value.asString());
	}
	
	@Test
	public void deveIdentificarCpfInvalido() {
		String padrao = "iscpf(\"  09.270.850/0001-84   \")";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("false", value.asString());
		assertFalse(value.asBoolean());
	}
	
	@Test
	public void deveIdentificarCpfInvalidoSemPontuacao() {
		String padrao = "iscpf(\"  12345678900   \")";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("false", value.asString());
		assertFalse(value.asBoolean());
	}
	
	@Test
	public void deveFormatarCnpjValidoComPontuacao() {
		String padrao = "cnpj(\"  09.270.850/0001-84   \")";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("09.270.850/0001-84", value.asString());
	}
	
	@Test
	public void deveFormatarCnpjValidoSemPontuacao() {
		String padrao = "cnpj(\"  09270850000184   \")";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("09.270.850/0001-84", value.asString());
	}
	
	@Test
	public void deveFormatarCnpjInvalidoSemPontuacao() {
		String padrao = "cnpj(\"  12345678901234   \")";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("12.345.678/9012-34", value.asString());
	}
	
	@Test
	public void naoDeveFormatarCnpjInvalidoMaiorDoQueQuatorzeNumerosSemPontuacao() {
		String padrao = "cnpj(\"  123456789012345   \")";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("123456789012345", value.asString());
	}
	
	@Test
	public void naoDeveFormatarCnpjInvalidoMenorDoQueQuatorzeNumerosSemPontuacao() {
		String padrao = "cnpj(\"  1234567890123   \")";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("1234567890123", value.asString());
	}
	
	@Test
	public void deveIdentificarCnpjValidoSemPontuacao() {
		String padrao = "iscnpj(\"  35290178000107   \")";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("true", value.asString());
		assertTrue(value.asBoolean());
	}
	
	@Test
	public void deveIdentificarCnpjValidoComPontuacao() {
		String padrao = "iscnpj(\"  35.290.178/0001-07   \")";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("true", value.asString());
		assertTrue(value.asBoolean());
	}
	
	@Test
	public void deveIdentificarCnpjInvalidoSemPontuacao() {
		String padrao = "iscnpj(\"  35290178000100   \")";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("false", value.asString());
		assertFalse(value.asBoolean());
	}
	
	@Test
	public void deveIdentificarCnpjInvalidoComPontuacao() {
		String padrao = "iscnpj(\"  35.290.178/0001-00   \")";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("false", value.asString());
		assertFalse(value.asBoolean());
	}
	
	@Test
	public void deveIdentificarCnpjInvalidoComJsonEIf() {
		String padrao = "iscnpj(json(\"$.cpf\")) ? cnpj(json(\"$.cpf\")) : cpf(json(\"$.cpf\"))";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals(new CPF(getCliente().getCpf()).getNumeroFormatado(), value.asString());
	}
	
	@Test
	public void deveRetornarMesmoConteudoQuandoJsonNaoTiverCifrao() {
		String padrao = "json(\"Meu nome é João\")";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("Meu nome é João", value.asString());
	}
	
	@Test
	public void deveIdentificarCnpjECpfInvalido() {
		String padrao = "iscnpj(json(\"$.cliente.cpf\")) ? cnpj(json(\"$.cliente.cpf\")) : [iscpf(json(\"$.cliente.cpf\")) ? cpf(json(\"$.cliente.cpf\")) : \"invalido\" ]";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("invalido", value.asString());
	}
	
	@Test
	public void deveIdentificarCnpjInvalidoECpfValido() {
		String padrao = "iscnpj(json(\"$.cpf\")) ? cnpj(json(\"$.cpf\")) : [iscpf(json(\"$.cpf\")) ? cpf(json(\"$.cpf\")) : \"invalido\" ]";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals(new CPF(getCliente().getCpf()).getNumeroFormatado(), value.asString());
	}
	
	@Test
	public void deveTratarData() {
		
		String data = "30-10-2023";
		String mascara_entrada = "dd-MM-yyyy";
		String mascara_saida = "ddMMyyyy";
		String data_esperada = "30102023";
		
		
		String padrao = "date(\" " + data +"\", \" " + mascara_entrada +   "\", \"" +  mascara_saida + "\", \"pt-br\")";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals(data_esperada, value.asString());
	}
	
	@Test
	public void deveConverterParaNumeroDecimal() {
		
		String data = "200.42";
		String padrao = "tonumber(\" " + data +" \")";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals(data, value.asString());
		assertTrue(new BigDecimal(value.asString()).equals(value.asDecimal()));
	}
	
	@Test
	public void deveConverterParaNumeroSemDecimal() {
		
		String data = "200";
		String padrao = "tonumber(\" " + data +" \")";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals(data, value.asString());
		assertTrue(new BigDecimal(value.asString()).equals(value.asDecimal()));
	}
	
	@Test
	public void deveRetornarExceptionQuandoTentarConverterStringNumero() {
		
		String data = "200k";
		String padrao = "tonumber(\"" + data +"\")";

		Exception exception = assertThrows(ParseCancellationException.class, () -> {
			Value value = chamarVisitor(padrao, getCliente());
		});
		
		assertEquals("Não é possível converter o valor para Numero :: 200k", exception.getMessage());
	}
	
	@Test
	public void deveTratarDataComEspacosNaSaida() {
		
		String data = "30-10-2023";
		String mascara_entrada = "dd-MM-yyyy";
		String mascara_saida = "dd MM yyyy";
		String data_esperada = "30 10 2023";
		
		
		String padrao = "date(\" " + data +"\", \" " + mascara_entrada +   "\", \"" +  mascara_saida + "\", \"pt-br\" )";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals(data_esperada, value.asString());
	}
	
	@Test
	public void deveTratarDataComEntradaPadraoAmericano() {
		
		String data = "2023-10-20";
		String mascara_entrada = "yyyy-MM-dd";
		String mascara_saida = "dd/MM/yyyy";
		String data_esperada = "20/10/2023";
		
		
		String padrao = "date(\" " + data +"\", \" " + mascara_entrada +   "\", \"" +  mascara_saida + "\" , \"pt-br\" )";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals(data_esperada, value.asString());
	}
	
	@Test
	public void deveTratarDataComEntradaPadraoAmericanoSemTraco() {
		
		String data = "20231020";
		String mascara_entrada = "yyyyMMdd";
		String mascara_saida = "dd/MM/yyyy";
		String data_esperada = "20/10/2023";
		
		String padrao = "date(\" " + data +"\", \" " + mascara_entrada +   "\", \"" +  mascara_saida + "\", \"pt-br\" )";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals(data_esperada, value.asString());
	}
	
	@Test
	public void deveTratarDataComEntradaPadraoAmericanoSemTracoComHora() {
		
		String data = "20231020 20:15:15";
		String mascara_entrada = "yyyyMMdd HH:mm:ss";
		String mascara_saida = "dd/MM/yyyy HH:mm";
		String data_esperada = "20/10/2023 20:15";

		String padrao = "date(\" " + data +"\", \" " + mascara_entrada +   "\", \"" +  mascara_saida + "\", \"pt-br\" )";
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals(data_esperada, value.asString());
	}
	
	@Test
	public void deveGerarExceptionQuandoDataInvalida() {
		
		String data = "2023XX20 20:15:15";
		String mascara_entrada = "yyyyMMdd HH:mm:ss";
		String mascara_saida = "dd/MM/yyyy HH:mm";
		String padrao = "date(\" " + data +"\", \" " + mascara_entrada +   "\", \"" +  mascara_saida + "\", \"pt-br\" )";
		
		Exception exception = assertThrows(ParseCancellationException.class, () -> {
			Value value = chamarVisitor(padrao, getCliente());
		});
		
		assertEquals("nao foi possivel converter para data ::  2023XX20 20:15:15", exception.getMessage());
		
	}
	
	@Test
	public void deveGerarExceptionQuandoInformarLinguagemForaDoPadrao() {
		
		String data = "2023XX20 20:15:15";
		String mascara_entrada = "yyyyMMdd HH:mm:ss";
		String mascara_saida = "dd/MM/yyyy HH:mm";
		String padrao = "date(\" " + data +"\", \" " + mascara_entrada +   "\", \"" +  mascara_saida + "\", \"ptbr\" )";
		
		Exception exception = assertThrows(ParseCancellationException.class, () -> {
			Value value = chamarVisitor(padrao, getCliente());
		});
		
		assertEquals("linguagem e pais deve ter o padrao \"pt-br\".", exception.getMessage());
		
	}
	
	@Test
	public void deveGerarExceptionQuandoInformarLinguagemForaDoPadraoComMaisDeUmTraco() {
		
		String data = "2023XX20 20:15:15";
		String mascara_entrada = "yyyyMMdd HH:mm:ss";
		String mascara_saida = "dd/MM/yyyy HH:mm";
		String padrao = "date(\" " + data +"\", \" " + mascara_entrada +   "\", \"" +  mascara_saida + "\", \"p-t-br\" )";
		
		Exception exception = assertThrows(ParseCancellationException.class, () -> {
			Value value = chamarVisitor(padrao, getCliente());
		});
		
		assertEquals("linguagem e pais deve ter o padrao \"pt-br\".", exception.getMessage());
		
	}
	
	@Test
	public void deveGerarExceptionQuandoInformarDataNaoString() {
		
		String mascara_entrada = "yyyyMMdd HH:mm:ss";
		String mascara_saida = "dd/MM/yyyy HH:mm";
		String padrao = "date( 20210321, \" " + mascara_entrada +   "\", \"" +  mascara_saida + "\", \"p-t-br\" )";
		
		Exception exception = assertThrows(ParseCancellationException.class, () -> {
			Value value = chamarVisitor(padrao, getCliente());
		});
		
		assertEquals("valor deve ser string :: 20210321", exception.getMessage());
		
		
		String padrao2 = "date( true, \" " + mascara_entrada +   "\", \"" +  mascara_saida + "\", \"p-t-br\" )";
		
		Exception exception2 = assertThrows(ParseCancellationException.class, () -> {
			Value value = chamarVisitor(padrao2, getCliente());
		});
		
		assertEquals("valor deve ser string :: true", exception2.getMessage());
		
	}
	
	@Test
	public void naoDeveCompararNumeroComTexto() {
		
		String padrao = " 1 >= \"dois\" ";
		
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("", value.asString());
		
		padrao = " \"dois\" < 5 ";
		value = chamarVisitor(padrao, getCliente());
		
		assertEquals("", value.asString());
		
	}
	
	@Test
	public void naoDeveCompararRelacionalParaTexto() {
		
		String padrao = " \"um\" >= \"dois\" ";
		
		Value value = chamarVisitor(padrao, getCliente());
		assertEquals("", value.asString());
		
	}		
}
