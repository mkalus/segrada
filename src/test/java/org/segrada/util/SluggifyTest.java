package org.segrada.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class SluggifyTest {
	@Test
	public void shouldSluglifySomeText() {
		assertEquals("schoene-neue-roehrenjeans-in-groesse-42", Sluggify.sluggify("Schöne neue Röhrenjeans in Größe 42"));
	}

	@Test
	public void shouldSluglifySomeTextWithLeadingAndTrailingHyphen() {
		assertEquals("schoene-neue-roehrenjeans-in-groesse-42", Sluggify.sluggify("Schöne neue Röhrenjeans in Größe 42-"));
	}

	@Test
	public void shouldSluggifyAmpersandCharacters() {
		assertEquals("roehrenjeans-waesche", Sluggify.sluggify("Röhrenjeans & Wäsche"));
	}

	@Test
	public void shouldSluggifyLeetHaxorSpeak() {
		assertEquals("leet-haxor", Sluggify.sluggify("-ĿēĚt^ħĂxôŔ-"));
	}

	@Test
	public void shouldRemoveSuccessiveHyphens() {
		assertEquals("le-et-hax-or", Sluggify.sluggify("Ŀē---Ět^-^ħĂx...ôŔ"));
	}

	@Test
	public void shouldNotReplaceUnderscores() {
		assertEquals("under_score", Sluggify.sluggify("Under_Score"));
	}

	@Test
	public void shouldRemoveOpastrophesBeforeLetterS() {
		assertEquals("moms-pants", Sluggify.sluggify("Mom's pants"));
	}

	@Test
	public void shouldReplaceOpastrophesWithDashIfNotFollowedByLetterS() {
		assertEquals("turtles-pants-are-awkward", Sluggify.sluggify("Turtles'pants are awkward'"));
	}

	@Test
	public void shouldSluggifyOtherSpecialLetters() {
		assertEquals("tsssssuwyzg-hijknsuwyz", Sluggify.sluggify("ťśšşŝșùŵýžĜ ĤÌĴĶÑŚÙŴÝŹ"));
	}

	@Test
	public void shouldSlugifyNullsAndEmptyStrings() {
		assertNull(Sluggify.sluggify(null));
		assertEquals("", Sluggify.sluggify(""));
	}

	@Test
	public void slugifyPlusSymbol() {
		assertEquals("aplus", Sluggify.sluggify("A+"));
		assertEquals("aplusplus", Sluggify.sluggify("A++"));
		assertEquals("aplusplusplus", Sluggify.sluggify("A+++"));
	}

	@Test
	public void slugifyMitLetztesZeichenAlsBindestrich() {
		assertEquals("david-schreibt-auch-einen-test", Sluggify.sluggify("David-schreibt-auch-einen-Test-"));
	}

	@Test
	public void slugifyMitSonderzeichen() {
		assertEquals("haup-t-hose_plus-un-d-so-wahns-i-n-n", Sluggify.sluggify("Haup(t)hose_+*~#'/-\"'un[d]so--Wahns{i}n.n;"));
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