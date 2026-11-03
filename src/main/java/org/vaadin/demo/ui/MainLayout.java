package org.vaadin.demo.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;

/** The frame around every view: title in the navbar, navigation in the drawer. */
public class MainLayout extends AppLayout {

    public MainLayout() {
        addToNavbar(new DrawerToggle(), new H1("Expenses"));
        addToDrawer(createNavigation());
    }

    private SideNav createNavigation() {
        var nav = new SideNav();
        nav.addItem(new SideNavItem("Übersicht", DashboardView.class, VaadinIcon.DASHBOARD.create()));
        nav.addItem(new SideNavItem("Ausgaben", ExpensesView.class, VaadinIcon.WALLET.create()));
        nav.addItem(new SideNavItem("Compliance-Regeln", RulesView.class, VaadinIcon.CLIPBOARD_CHECK.create()));
        return nav;
    }
}
