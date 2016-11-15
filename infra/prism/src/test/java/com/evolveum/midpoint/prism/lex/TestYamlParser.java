package com.evolveum.midpoint.prism.lex;

import com.evolveum.midpoint.prism.lex.json.YamlLexicalProcessor;

public class TestYamlParser extends AbstractLexicalProcessorTest {
 
	
	@Override
	protected String getSubdirName() {
		return "yaml";
	}

	@Override
	protected String getFilenameSuffix() {
		return "yaml";
	}

	@Override
	protected YamlLexicalProcessor createParser() {
		return new YamlLexicalProcessor();
	}

	@Override
	protected String getWhenItemSerialized() {
		return "when: \"2012-02-24T10:48:52.000Z\"";
	}
}
