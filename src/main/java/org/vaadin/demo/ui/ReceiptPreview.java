package org.vaadin.demo.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.ai.common.Rect;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.IFrame;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.Base64;
import java.util.List;

/**
 * Shows the receipt that was just uploaded: an image as picture, a PDF in a frame. On an image,
 * {@link #highlight} marks the places the AI read a value from.
 */
public class ReceiptPreview extends Composite<VerticalLayout> {

    private final Div highlights = new Div();

    public ReceiptPreview() {
        VerticalLayout layout = getContent();
        layout.setPadding(false);
        layout.setWidth("28rem");
        highlights.addClassName("receipt-highlights");
        setVisible(false);
    }

    public void show(byte[] data, String mimeType) {
        String dataUrl = "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(data);
        VerticalLayout layout = getContent();
        layout.removeAll();
        highlights.removeAll();
        layout.add(createViewer(dataUrl, mimeType));
        setVisible(true);
    }

    public void clear() {
        getContent().removeAll();
        highlights.removeAll();
        setVisible(false);
    }

    /**
     * Marks these places on the image, replacing earlier marks. The rectangles are fractions (0..1) of
     * the image size, from the top-left corner, so they fit at any display size. No marks for a PDF:
     * its pages are not rendered by us.
     */
    public void highlight(List<Rect> places) {
        highlights.removeAll();
        places.forEach(place -> highlights.add(createMark(place)));
    }

    private Component createViewer(String dataUrl, String mimeType) {
        if ("application/pdf".equals(mimeType)) {
            var frame = new IFrame(dataUrl);
            frame.setSizeFull();
            return frame;
        }
        var image = new Image(dataUrl, "Uploaded receipt");
        image.setMaxWidth("100%");
        var wrapper = new Div(image, highlights);
        wrapper.addClassName("receipt-image");
        return wrapper;
    }

    /** The position goes in as CSS variables; styles.css turns them into a frame over the image. */
    private Div createMark(Rect place) {
        var mark = new Div();
        mark.addClassName("receipt-highlight");
        mark.getStyle().set("--x", String.valueOf(place.x()));
        mark.getStyle().set("--y", String.valueOf(place.y()));
        mark.getStyle().set("--w", String.valueOf(place.width()));
        mark.getStyle().set("--h", String.valueOf(place.height()));
        return mark;
    }
}
