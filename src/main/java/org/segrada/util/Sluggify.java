package org.segrada.util;

import com.github.slugify.Slugify;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import java.text.Normalizer;
import java.util.HashMap;

/**
 * Copyright 2015 Otto.de - https://github.com/otto-de/sluggify
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
 * Sluggify library - taken from https://github.com/otto-de/sluggify
 * and adapted
 */
public final class Sluggify {
    // slugify instances with custom replacements
    private static final Slugify slugger = new Slugify().withCustomReplacements(new HashMap<String, String>() {{
        put("ħ", "h");
        put("+", "plus");
        put("'s", "s");
        put("'S", "s");
        put("ƒ", "f");
        put("€", "eur");
        put("Ħ", "h");
        put("ĸ", "k");
        put("Ŋ", "n");
        put("ŋ", "n");
        put("Þ", "th");
        put("þ", "th");
        put("ŧ", "t");
        put("Đ", "dh");
        put("Ð", "dh");
        put("đ", "dh");
        put("ð", "dh");
    }});
    private static final Slugify asciifier = new Slugify().withCustomReplacements(new HashMap<String, String>() {{
        put("ä", "a");
        put("Ä", "a");
        put("ö", "o");
        put("Ö", "o");
        put("ü", "u");
        put("Ü", "u");
        put("ß", "s");
        put("ħ", "h");
        put("+", "plus");
        put("'s", "s");
        put("'S", "s");
        put("Å", "a");
        put("å", "a");
        put("ƒ", "f");
        put("€", "eur");
        put("Ħ", "h");
        put("ĸ", "k");
        put("Ŋ", "n");
        put("ŋ", "n");
        put("ø", "o");
        put("Ø", "o");
        put("Þ", "th");
        put("þ", "th");
        put("ŧ", "t");
        put("Đ", "d");
        put("Ð", "d");
        put("đ", "d");
        put("ð", "d");
    }});

    static {
        //Create a singleton CacheManager using defaults
        CacheManager manager = CacheManager.getInstance();

        // create slug caches - in memory cache
        Cache slugCache = new Cache(
                new CacheConfiguration("slugCache", 10000)
                        .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
                        .eternal(false)
                        .timeToLiveSeconds(600)
                        .timeToIdleSeconds(300)
                        .diskExpiryThreadIntervalSeconds(0));
        Cache asciiCache = new Cache(
                new CacheConfiguration("asciiCache", 10000)
                        .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
                        .eternal(false)
                        .timeToLiveSeconds(600)
                        .timeToIdleSeconds(300)
                        .diskExpiryThreadIntervalSeconds(0));

        manager.addCache(slugCache);
        manager.addCache(asciiCache);
    }

    private static final Ehcache slugCache = CacheManager.getInstance().getEhcache("slugCache");
    private static final Ehcache asciiCache = CacheManager.getInstance().getEhcache("asciiCache");

    private Sluggify() throws InstantiationException {
        throw new InstantiationException("The class is not created for instantiation");
    }

    public static boolean isEmpty(String stringToCheck) {
        return stringToCheck == null || stringToCheck.isEmpty();
    }

    /**
     * Sluggify string - transliterating umlauts and other special characters, e.g. ö to oe
     * @param string input string
     * @return slug
     */
    public static String sluggify(String string) {
        return doSlug(string, slugger, slugCache);
    }

    /**
     * Strict asciification of input, oe will become o
     * @param string input string
     * @return slug
     */
    public static String asciify(String string) {
        return doSlug(string, asciifier, asciiCache);
    }

    /**
     * actual worker
     * @param string input string
     * @param slugifier Slugify instance to use
     * @param cache cache to use
     * @return slug
     */
    private static String doSlug(String string, Slugify slugifier, Ehcache cache) {
        if (isEmpty(string)) {
            return string;
        }

        // get from cache
        Element cacheHit = cache.get(string);
        if (cacheHit != null) {
            return cacheHit.getObjectValue().toString();
        }

        String slug = slugifier.slugify(string);

        // to cache
        cache.put(new Element(string, slug));

        return slug;
    }

    /**
     * Normalize input
     *
     * @param string to be normalized and trimmed
     * @return normalized input
     */
    public static String normalize(String string) {
        if (isEmpty(string)) {
            return string;
        }

        return Normalizer.normalize(string, Normalizer.Form.NFC).trim();
    }
}
