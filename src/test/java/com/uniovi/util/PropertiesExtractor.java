package com.uniovi.util;

import lombok.Getter;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.ResourceBundle;

public class PropertiesExtractor {

	static private String Path;
	@Getter
	static final int SPANISH = 0;
	@Getter
	static final int ENGLISH = 1;
	@Getter
	static final int FRENCH = 2;

	static final Locale[] idioms = new Locale[]{Locale.forLanguageTag(
			"es-ES"), Locale.forLanguageTag("en-US"), Locale.forLanguageTag("fr-FR")};

	public PropertiesExtractor(String Path) {
		PropertiesExtractor.Path = Path;
	}

	public String getString(String prop, int locale) {
		ResourceBundle bundle = ResourceBundle.getBundle(Path, idioms[locale]);
		String value = bundle.getString(prop);
		String result;
		result = new String(value.getBytes(StandardCharsets.UTF_8),
							StandardCharsets.UTF_8);
		return result;
	}

}
