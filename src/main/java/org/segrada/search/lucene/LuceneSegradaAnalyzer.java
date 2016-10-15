package org.segrada.search.lucene;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;

import java.io.IOException;
import java.io.Reader;

/**
 * Copyright 2016 Maximilian Kalus [segrada@auxnet.de]
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * Search engine: Lucene Analyzer which also folds ASCII
 */
public class LuceneSegradaAnalyzer extends StopwordAnalyzerBase {
	//TODO add test method

	@Override
	protected TokenStreamComponents createComponents(final String fieldName) {
		final Tokenizer src;
		StandardTokenizer t = new StandardTokenizer();
		t.setMaxTokenLength(255);
		src = t;

		TokenStream tok = new StandardFilter(src);
		tok = new LowerCaseFilter(tok);
		tok = new StopFilter(tok, stopwords);
		tok = new ASCIIFoldingFilter(tok); // added this
		return new TokenStreamComponents(src, tok) {
			@Override
			protected void setReader(final Reader reader) {
				((StandardTokenizer)src).setMaxTokenLength(255);
				try {
					super.setReader(reader);
				} catch (IOException e) {
					//TODO: what to do here?
				}
			}
		};
	}
}
