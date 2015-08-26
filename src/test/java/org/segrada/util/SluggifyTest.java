package org.segrada.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class SluggifyTest {
	@Test
	public void shouldSluglifySomeText() {
		assertEquals("schoene-neue-roehrenjeans-in-groesse-42", Sluggify.sluggify("Schöne neue Röhrenjeans in Größe 42"));
		assertEquals("schone-neue-rohrenjeans-in-grose-42", Sluggify.asciify("Schöne neue Röhrenjeans in Größe 42"));
	}

	@Test
	public void shouldSluglifySomeTextWithLeadingAndTrailingHyphen() {
		assertEquals("schoene-neue-roehrenjeans-in-groesse-42", Sluggify.sluggify("Schöne neue Röhrenjeans in Größe 42-"));
		assertEquals("schone-neue-rohrenjeans-in-grose-42", Sluggify.asciify("Schöne neue Röhrenjeans in Größe 42-"));
	}

	@Test
	public void shouldSluggifyAmpersandCharacters() {
		assertEquals("roehrenjeans-waesche", Sluggify.sluggify("Röhrenjeans & Wäsche"));
		assertEquals("rohrenjeans-wasche", Sluggify.asciify("Röhrenjeans & Wäsche"));
	}

	@Test
	public void shouldSluggifyLeetHaxorSpeak() {
		assertEquals("leet-haxor", Sluggify.sluggify("-ĿēĚt^ħĂxôŔ-"));
		assertEquals("leet-haxor", Sluggify.asciify("-ĿēĚt^ħĂxôŔ-"));
	}

	@Test
	public void shouldRemoveSuccessiveHyphens() {
		assertEquals("le-et-hax-or", Sluggify.sluggify("Ŀē---Ět^-^ħĂx...ôŔ"));
		assertEquals("le-et-hax-or", Sluggify.asciify("Ŀē---Ět^-^ħĂx...ôŔ"));
	}

	@Test
	public void shouldNotReplaceUnderscores() {
		assertEquals("under_score", Sluggify.sluggify("Under_Score"));
		assertEquals("under_score", Sluggify.asciify("Under_Score"));
	}

	@Test
	public void shouldRemoveOpastrophesBeforeLetterS() {
		assertEquals("moms-pants", Sluggify.sluggify("Mom's pants"));
		assertEquals("moms-pants", Sluggify.asciify("Mom's pants"));
	}

	@Test
	public void shouldReplaceOpastrophesWithDashIfNotFollowedByLetterS() {
		assertEquals("turtles-pants-are-awkward", Sluggify.sluggify("Turtles'pants are awkward'"));
		assertEquals("turtles-pants-are-awkward", Sluggify.asciify("Turtles'pants are awkward'"));
	}

	@Test
	public void shouldSluggifyOtherSpecialLetters() {
		assertEquals("tsssssuwyzg-hijknsuwyz", Sluggify.sluggify("ťśšşŝșùŵýžĜ ĤÌĴĶÑŚÙŴÝŹ"));
		assertEquals("tsssssuwyzg-hijknsuwyz", Sluggify.asciify("ťśšşŝșùŵýžĜ ĤÌĴĶÑŚÙŴÝŹ"));
	}

	@Test
	public void shouldSlugifyNullsAndEmptyStrings() {
		assertNull(Sluggify.sluggify(null));
		assertEquals("", Sluggify.sluggify(""));

		assertNull(Sluggify.asciify(null));
		assertEquals("", Sluggify.asciify(""));
	}

	@Test
	public void slugifyPlusSymbol() {
		assertEquals("aplus", Sluggify.sluggify("A+"));
		assertEquals("aplusplus", Sluggify.sluggify("A++"));
		assertEquals("aplusplusplus", Sluggify.sluggify("A+++"));

		assertEquals("aplus", Sluggify.asciify("A+"));
		assertEquals("aplusplus", Sluggify.asciify("A++"));
		assertEquals("aplusplusplus", Sluggify.asciify("A+++"));
	}

	@Test
	public void slugifyMitLetztesZeichenAlsBindestrich() {
		assertEquals("david-schreibt-auch-einen-test", Sluggify.sluggify("David-schreibt-auch-einen-Test-"));
		assertEquals("david-schreibt-auch-einen-test", Sluggify.asciify("David-schreibt-auch-einen-Test-"));
	}

	@Test
	public void slugifyMitSonderzeichen() {
		assertEquals("haup-t-hose_plus-un-d-so-wahns-i-n-n", Sluggify.sluggify("Haup(t)hose_+*~#'/-\"'un[d]so--Wahns{i}n.n;"));
		assertEquals("haup-t-hose_plus-un-d-so-wahns-i-n-n", Sluggify.asciify("Haup(t)hose_+*~#'/-\"'un[d]so--Wahns{i}n.n;"));
	}

	@Test
	public void shouldSluggifyCategoryPath() {
		// given
		String categoryTitle = "Mode>Jungen>Mädchen>Schnürschuhe";

		// when
		String path = Sluggify.sluggifyPath(categoryTitle, ">", "/");

		assertEquals("mode/jungen/maedchen/schnuerschuhe", path.toString());
	}

	@Test
	public void shouldSluggifyCategoryPathWithOnePathElement() {
		// given
		String categoryTitle = "Mode";

		// when
		String path = Sluggify.sluggifyPath(categoryTitle, ">", "/");

		// then
		assertEquals("mode", path.toString());
	}

	@Test
	public void shouldSluggifyCategoryPathWithTwoEmptyPathElements() {
		// given
		String categoryTitle = "Mode>>";

		// when
		String path = Sluggify.sluggifyPath(categoryTitle, ">", "/");

		// then
		assertEquals("mode//", path.toString());
	}
}