package miayeelight.ux.schermo;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.util.regex.Pattern;

public class FiltroEspressioneRegolare extends DocumentFilter {

    public static final FiltroEspressioneRegolare FILTRO_TEMPI = new FiltroEspressioneRegolare("^[0-9]*$");

    private final Pattern pattern;

    public FiltroEspressioneRegolare(final String regex) {
        this.pattern = Pattern.compile(regex);
    }

    @Override
    public void insertString(final FilterBypass fb, int offset, final String string, final AttributeSet attr) throws BadLocationException {
        final StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
        sb.insert(offset, string);

        if (pattern.matcher(sb.toString()).matches()) {
            super.insertString(fb, offset, string, attr);
        }
    }

    @Override
    public void replace(final FilterBypass fb, int offset, int length, final String text, final AttributeSet attrs) throws BadLocationException {
        final StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
        sb.replace(offset, offset + length, text);

        if (pattern.matcher(sb.toString()).matches()) {
            super.replace(fb, offset, length, text, attrs);
        }
    }

    @Override
    public void remove(final FilterBypass fb, int offset, int length) throws BadLocationException {
        final StringBuilder sb = new StringBuilder(fb.getDocument().getText(0, fb.getDocument().getLength()));
        sb.delete(offset, offset + length);

        if (pattern.matcher(sb.toString()).matches()) {
            super.remove(fb, offset, length);
        }
    }
}