package org.segrada.util;

import net.sf.ehcache.CacheManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
	public void shouldSluggifyUmlautSpecialLetters() {
		assertEquals("aeaeaeoeoeueuess", Sluggify.sluggify("äÄæöÖÜüß"));
		assertEquals("aaaeoouus", Sluggify.asciify("äÄæöÖÜüß"));
	}

	@Test
	public void shouldSluggifyASpecialLetters() {
		assertEquals("aaaaaaaaaaaaaaaaaa", Sluggify.sluggify("ÀÁÂÃÅĀĄĂàáâãåāąă"));
		assertEquals("aaaaaaaaaaaaaaaa", Sluggify.asciify("ÀÁÂÃÅĀĄĂàáâãåāąă"));
	}

	@Test
	public void shouldSluggifyCSpecialLetters() {
		assertEquals("cccccccccc", Sluggify.sluggify("çćčĉċÇĆČĈĊ"));
		assertEquals("cccccccccc", Sluggify.asciify("çćčĉċÇĆČĈĊ"));
	}

	@Test
	public void shouldSluggifyDSpecialLetters() {
		assertEquals("ddhdhddhdh", Sluggify.sluggify("ďđðĎĐÐ"));
		assertEquals("dddddd", Sluggify.asciify("ďđðĎĐÐ"));
	}

	@Test
	public void shouldSluggifyESpecialLetters() {
		assertEquals("eeeeeeeeeeeeeeeeee", Sluggify.sluggify("ÈÉÊËĒĘĚĔĖèéêëēęěĕė"));
		assertEquals("eeeeeeeeeeeeeeeeee", Sluggify.asciify("ÈÉÊËĒĘĚĔĖèéêëēęěĕė"));
	}

	@Test
	public void shouldSluggifyGSpecialLetters() {
		assertEquals("gggggggg", Sluggify.sluggify("ĠĢĜĞĝğġģ"));
		assertEquals("gggggggg", Sluggify.asciify("ĠĢĜĞĝğġģ"));
	}

	@Test
	public void shouldSluggifyHSpecialLetters() {
		assertEquals("hhhh", Sluggify.sluggify("ĤĦĥħ"));
		assertEquals("hhhh", Sluggify.asciify("ĤĦĥħ"));
	}

	@Test
	public void shouldSluggifyISpecialLetters() {
		assertEquals("iiiiiiiiiiiiiiiiii", Sluggify.sluggify("ÌÍÎÏĪĨĬĮİìíîïīĩĭįı"));
		assertEquals("iiiiiiiiiiiiiiiiii", Sluggify.asciify("ÌÍÎÏĪĨĬĮİìíîïīĩĭįı"));
	}

	@Test
	public void shouldSluggifyJSpecialLetters() {
		assertEquals("jj", Sluggify.sluggify("Ĵĵ"));
		assertEquals("jj", Sluggify.asciify("Ĵĵ"));
	}

	@Test
	public void shouldSluggifyKSpecialLetters() {
		assertEquals("kkk", Sluggify.sluggify("Ķķĸ"));
		assertEquals("kkk", Sluggify.asciify("Ķķĸ"));
	}

	@Test
	public void shouldSluggifyLSpecialLetters() {
		assertEquals("llllllllll", Sluggify.sluggify("łľĺļŀŁĽĹĻĿ"));
		assertEquals("llllllllll", Sluggify.asciify("łľĺļŀŁĽĹĻĿ"));
	}

	@Test
	public void shouldSluggifyNSpecialLetters() {
		assertEquals("nnnnnnnnnnn", Sluggify.sluggify("ÑŃŇŅŊñńňņŉŋ"));
		assertEquals("nnnnnnnnnnn", Sluggify.asciify("ÑŃŇŅŊñńňņŉŋ"));
	}

	@Test
	public void shouldSluggifyOSpecialLetters() {
		assertEquals("oooooeooooooooeooo", Sluggify.sluggify("òóôõøōőŏœÒÓÔÕØŌŐŎ"));
		assertEquals("oooooooooooooooo", Sluggify.asciify("òóôõøōőŏœÒÓÔÕØŌŐŎ"));
	}

	@Test
	public void shouldSluggifyTHSpecialLetters() {
		assertEquals("thth", Sluggify.sluggify("Þþ"));
		assertEquals("thth", Sluggify.asciify("Þþ"));
	}

	@Test
	public void shouldSluggifyRSpecialLetters() {
		assertEquals("rrrrrr", Sluggify.sluggify("ŕřŗŔŘŖ"));
		assertEquals("rrrrrr", Sluggify.asciify("ŕřŗŔŘŖ"));
	}

	@Test
	public void shouldSluggifySSpecialLetters() {
		assertEquals("ssssssssss", Sluggify.sluggify("ŚŠŞŜȘśšşŝș"));
		assertEquals("ssssssssss", Sluggify.asciify("ŚŠŞŜȘśšşŝș"));
	}

	@Test
	public void shouldSluggifyTSpecialLetters() {
		assertEquals("tttt", Sluggify.sluggify("ťţŧț"));
		assertEquals("tttt", Sluggify.asciify("ťţŧț"));
	}

	@Test
	public void shouldSluggifyUSpecialLetters() {
		assertEquals("uuuuuuuuuuuuuuuuuu", Sluggify.sluggify("ÙÚÛŪŮŰŬŨŲùúûūůűŭũų"));
		assertEquals("uuuuuuuuuuuuuuuuuu", Sluggify.asciify("ÙÚÛŪŮŰŬŨŲùúûūůűŭũų"));
	}

	@Test
	public void shouldSluggifyWSpecialLetters() {
		assertEquals("ww", Sluggify.sluggify("Ŵŵ"));
		assertEquals("ww", Sluggify.asciify("Ŵŵ"));
	}

	@Test
	public void shouldSluggifyYSpecialLetters() {
		assertEquals("yyyyyy", Sluggify.sluggify("ÝŶŸýÿŷ"));
		assertEquals("yyyyyy", Sluggify.asciify("ÝŶŸýÿŷ"));
	}

	@Test
	public void shouldSluggifyZSpecialLetters() {
		assertEquals("zzzzzz", Sluggify.sluggify("ŹŽŻžżź"));
		assertEquals("zzzzzz", Sluggify.asciify("ŹŽŻžżź"));
	}

	@Test
	public void shouldSluggifyOtherSpecialLetters() {
		assertEquals("fs-eurij-plus", Sluggify.sluggify("ƒſ €ĳ +"));
		assertEquals("fs-eurij-plus", Sluggify.asciify("ƒſ €ĳ +"));
	}

	@Test
	public void shouldSluggifyGreek() {
		assertEquals("ellhnika", Sluggify.sluggify("Ελληνικά"));
		assertEquals("ellhnika", Sluggify.asciify("Ελληνικά"));
	}

	/* TODO: This does not work yet...
	@Test
	public void shouldSluggifyChinese() {
		assertEquals("", Sluggify.sluggify("中华人民共和国"));
		assertEquals("", Sluggify.asciify("中华人民共和国"));
	}
	*/

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
}
