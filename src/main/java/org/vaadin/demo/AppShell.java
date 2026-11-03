package org.vaadin.demo;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.aura.Aura;

@StyleSheet(Aura.STYLESHEET)
@StyleSheet("styles.css") // Your custom styles
// Push lets the server update the browser at any time. Needed later, when the AI changes the UI from a background thread.
@Push
public class AppShell implements AppShellConfigurator {
}
