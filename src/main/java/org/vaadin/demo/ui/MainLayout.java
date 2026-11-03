package org.vaadin.demo.ui;

import com.vaadin.draiv.ai.presenter.TextPresenterProperties;
import com.vaadin.draiv.ai.presenter.TextPresenterService;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import org.vaadin.demo.ui.chat.ChatDialog;

/** The frame around every view: title and chat button in the navbar, navigation in the drawer. */
public class MainLayout extends AppLayout {

    private final ChatDialog chat;

    public MainLayout(TextPresenterService presenter, TextPresenterProperties aiProperties) {
        this.chat = new ChatDialog(presenter);
        var title = new H1("Expenses");
        title.addClassName("app-title");
        addToNavbar(new DrawerToggle(), title, createChatButton(aiProperties.isEnabled()));
        addToDrawer(createNavigation());
    }

    @Override
    protected void onDetach(DetachEvent event) {
        chat.shutdown();
    }

    private Button createChatButton(boolean aiAvailable) {
        var button = new Button("Chat", VaadinIcon.COMMENT_ELLIPSIS.create(), e -> chat.toggle());
        button.setEnabled(aiAvailable);
        if (!aiAvailable) {
            button.setTooltipText("Kein OPENAI_API_KEY gesetzt");
        }
        return button;
    }

    private SideNav createNavigation() {
        var nav = new SideNav();
        nav.addItem(new SideNavItem("Übersicht", DashboardView.class, VaadinIcon.DASHBOARD.create()));
        nav.addItem(new SideNavItem("Ausgaben", ExpensesView.class, VaadinIcon.WALLET.create()));
        nav.addItem(new SideNavItem("Compliance-Regeln", RulesView.class, VaadinIcon.CLIPBOARD_CHECK.create()));
        return nav;
    }
}
