import static java.util.Map.entry;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.ro.RomanianAnalyzer;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.tartarus.snowball.ext.RomanianStemmer;

import java.io.IOException;
import java.util.Map;

final class DiacriticsFilter extends TokenFilter {
    CharTermAttribute charTermAttribute;
    private final Map<Character, Character> DIACRITICS_TO_PLAIN_LETTER =
            Map.ofEntries(
                    entry('ă','a'),
    entry('â','a'), entry('î','i'), entry('ș','s'),  entry('ş','s'),
                    entry('ţ', 't'), entry('ț', 't')
            );

    protected DiacriticsFilter(TokenStream input) {
        super(input);
        this.charTermAttribute = addAttribute(CharTermAttribute.class);
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (!input.incrementToken()) {
            return false;
        }

        char[] buffer = charTermAttribute.buffer();
        char[] newBuffer = removeDiacritics(buffer);
        charTermAttribute.setEmpty();
        charTermAttribute.copyBuffer(newBuffer, 0, newBuffer.length);
        return true;
    }

    private char[] removeDiacritics(char[] buffer) {
        int length = charTermAttribute.length();
        char[] newBuffer = new char[length];
        for (int i = 0; i < length; i++) {
            if (DIACRITICS_TO_PLAIN_LETTER.containsKey(buffer[i])) {
                newBuffer[i] = DIACRITICS_TO_PLAIN_LETTER.get(buffer[i]);
            } else {
                newBuffer[i] = buffer[i];
            }
        }
        return newBuffer;
    }
}

public class RoAnalyzer extends Analyzer {

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        StandardTokenizer src = new StandardTokenizer();
        TokenStream result = new LowerCaseFilter(src);
        result = new StopFilter(result,  RomanianAnalyzer.getDefaultStopSet());
        result = new SnowballFilter(result, new RomanianStemmer());
        result = new DiacriticsFilter(result);
        return new TokenStreamComponents(src, result);
    }
}