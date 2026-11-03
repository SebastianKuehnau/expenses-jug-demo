package org.vaadin.demo.ai;

import com.vaadin.draiv.ai.presenter.TextPresenterService.TurnEvent;
import java.util.Optional;

/** Turns what the AI is doing into the short German line shown in the chat while it works. */
public final class TurnStatus {

    private TurnStatus() {
    }

    /** The line for this event, or empty if it is not worth showing (e.g. every streamed token). */
    public static Optional<String> line(TurnEvent event) {
        return switch (event.type()) {
            case "model_request" -> Optional.of("Denke nach…");
            case "cancelled" -> Optional.of("Stoppe…");
            case "tool_call" -> Optional.of(toolLine(String.valueOf(event.data().get("name"))));
            default -> Optional.empty();
        };
    }

    private static String toolLine(String tool) {
        return switch (tool) {
            case "navigate" -> "Öffne die Seite…";
            case "inspect", "list_capabilities" -> "Schaue auf den Bildschirm…";
            case "apply", "apply_many" -> "Fülle aus / klicke…";
            default -> "Arbeite…";
        };
    }
}
