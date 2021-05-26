// Generated from /media/angelo/WorkData/git/A-Tool-for-Verifying-Strategic-Properties-in-MAS-with-Imperfect-Information/abstraction/src/main/antlr4/ATL.g4 by ANTLR 4.9.1
package fr.univ_evry.ibisc.atl.parser;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ATLLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, ATOM=23, WS=24;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"T__9", "T__10", "T__11", "T__12", "T__13", "T__14", "T__15", "T__16", 
			"T__17", "T__18", "T__19", "T__20", "T__21", "ATOM", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'!'", "'not'", "'next'", "'X'", "'eventually'", "'F'", "'always'", 
			"'G'", "'until'", "'U'", "'release'", "'R'", "'&&'", "'and'", "'||'", 
			"'or'", "'->'", "'implies'", "'<'", "'>'", "'('", "')'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, "ATOM", 
			"WS"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public ATLLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "ATL.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\32\u0096\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\3\2\3\2\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\6\3\6\3\6"+
		"\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3"+
		"\t\3\t\3\n\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3"+
		"\f\3\r\3\r\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\20\3\20\3\20\3\21\3\21"+
		"\3\21\3\22\3\22\3\22\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\24\3\24"+
		"\3\25\3\25\3\26\3\26\3\27\3\27\3\30\3\30\7\30\u008b\n\30\f\30\16\30\u008e"+
		"\13\30\3\31\6\31\u0091\n\31\r\31\16\31\u0092\3\31\3\31\2\2\32\3\3\5\4"+
		"\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\22"+
		"#\23%\24\'\25)\26+\27-\30/\31\61\32\3\2\5\5\2C\\aac|\6\2\62;C\\aac|\5"+
		"\2\13\f\16\17\"\"\2\u0097\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2"+
		"\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2"+
		"\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3"+
		"\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2"+
		"\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\3\63\3\2\2\2\5\65\3\2\2\2\79\3"+
		"\2\2\2\t>\3\2\2\2\13@\3\2\2\2\rK\3\2\2\2\17M\3\2\2\2\21T\3\2\2\2\23V\3"+
		"\2\2\2\25\\\3\2\2\2\27^\3\2\2\2\31f\3\2\2\2\33h\3\2\2\2\35k\3\2\2\2\37"+
		"o\3\2\2\2!r\3\2\2\2#u\3\2\2\2%x\3\2\2\2\'\u0080\3\2\2\2)\u0082\3\2\2\2"+
		"+\u0084\3\2\2\2-\u0086\3\2\2\2/\u0088\3\2\2\2\61\u0090\3\2\2\2\63\64\7"+
		"#\2\2\64\4\3\2\2\2\65\66\7p\2\2\66\67\7q\2\2\678\7v\2\28\6\3\2\2\29:\7"+
		"p\2\2:;\7g\2\2;<\7z\2\2<=\7v\2\2=\b\3\2\2\2>?\7Z\2\2?\n\3\2\2\2@A\7g\2"+
		"\2AB\7x\2\2BC\7g\2\2CD\7p\2\2DE\7v\2\2EF\7w\2\2FG\7c\2\2GH\7n\2\2HI\7"+
		"n\2\2IJ\7{\2\2J\f\3\2\2\2KL\7H\2\2L\16\3\2\2\2MN\7c\2\2NO\7n\2\2OP\7y"+
		"\2\2PQ\7c\2\2QR\7{\2\2RS\7u\2\2S\20\3\2\2\2TU\7I\2\2U\22\3\2\2\2VW\7w"+
		"\2\2WX\7p\2\2XY\7v\2\2YZ\7k\2\2Z[\7n\2\2[\24\3\2\2\2\\]\7W\2\2]\26\3\2"+
		"\2\2^_\7t\2\2_`\7g\2\2`a\7n\2\2ab\7g\2\2bc\7c\2\2cd\7u\2\2de\7g\2\2e\30"+
		"\3\2\2\2fg\7T\2\2g\32\3\2\2\2hi\7(\2\2ij\7(\2\2j\34\3\2\2\2kl\7c\2\2l"+
		"m\7p\2\2mn\7f\2\2n\36\3\2\2\2op\7~\2\2pq\7~\2\2q \3\2\2\2rs\7q\2\2st\7"+
		"t\2\2t\"\3\2\2\2uv\7/\2\2vw\7@\2\2w$\3\2\2\2xy\7k\2\2yz\7o\2\2z{\7r\2"+
		"\2{|\7n\2\2|}\7k\2\2}~\7g\2\2~\177\7u\2\2\177&\3\2\2\2\u0080\u0081\7>"+
		"\2\2\u0081(\3\2\2\2\u0082\u0083\7@\2\2\u0083*\3\2\2\2\u0084\u0085\7*\2"+
		"\2\u0085,\3\2\2\2\u0086\u0087\7+\2\2\u0087.\3\2\2\2\u0088\u008c\t\2\2"+
		"\2\u0089\u008b\t\3\2\2\u008a\u0089\3\2\2\2\u008b\u008e\3\2\2\2\u008c\u008a"+
		"\3\2\2\2\u008c\u008d\3\2\2\2\u008d\60\3\2\2\2\u008e\u008c\3\2\2\2\u008f"+
		"\u0091\t\4\2\2\u0090\u008f\3\2\2\2\u0091\u0092\3\2\2\2\u0092\u0090\3\2"+
		"\2\2\u0092\u0093\3\2\2\2\u0093\u0094\3\2\2\2\u0094\u0095\b\31\2\2\u0095"+
		"\62\3\2\2\2\5\2\u008c\u0092\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}