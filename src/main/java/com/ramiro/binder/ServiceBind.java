package com.ramiro.binder;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;

import com.ramiro.binder.generated.ComprovLexer;
import com.ramiro.binder.generated.ComprovParser;
import com.ramiro.binder.visitor.ComprovanteVisitor;
import com.ramiro.binder.visitor.ComprovanteVisitorErrorListener;

public class ServiceBind {

	public String bind(String padrao, Object object) throws ServiceBindException {

		try {
			return this.tratar(padrao, object);
		} catch (ParseCancellationException e) {
			throw new ServiceBindException("Erro ao tentar tratar o padrao: " + padrao, e);
		}

	}

	private String tratar(String padrao, Object object) {

		ComprovanteVisitorErrorListener error = new ComprovanteVisitorErrorListener();
		ComprovLexer lexer = new ComprovLexer(CharStreams.fromString(padrao));
		lexer.removeErrorListeners();
		lexer.addErrorListener(error);

		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ComprovParser parser = new ComprovParser(tokens);
		parser.removeErrorListeners();
		parser.addErrorListener(error);

		ParseTree tree = parser.programa();
		ComprovanteVisitor visitor = new ComprovanteVisitor(object);

		return visitor.visit(tree).asString();

	}
}
