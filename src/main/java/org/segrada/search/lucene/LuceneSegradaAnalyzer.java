package org.segrada.search.lucene;

import com.google.inject.Singleton;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.StopwordAnalyzerBase;
import org.apache.lucene.util.Version;

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

	/**
	 * Constructor
	 */
	public LuceneSegradaAnalyzer() {
		super(Version.LUCENE_47, StandardAnalyzer.STOP_WORDS_SET);
	}

	@Override
	protected TokenStreamComponents createComponents(String s, Reader reader) {
		final StandardTokenizer src = new StandardTokenizer(this.matchVersion, reader);
		src.setMaxTokenLength(255);

		StandardFilter tok = new StandardFilter(this.matchVersion, src);
		LowerCaseFilter tok1 = new LowerCaseFilter(this.matchVersion, tok);
		StopFilter tok2 = new StopFilter(this.matchVersion, tok1, this.stopwords);
		final ASCIIFoldingFilter tok3 = new ASCIIFoldingFilter(tok2);

		return new TokenStreamComponents(src, tok3) {
			protected void setReader(Reader reader) throws IOException {
				src.setMaxTokenLength(255);
				super.setReader(reader);
			}
		};
	}
}
