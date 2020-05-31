package org.nefele.ui.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.MenuItem;
import org.nefele.Mime;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class FileBrowserItem {

    private final ObjectProperty<Mime> mime;
    private final StringProperty text;
    private List<MenuItem> menuItems;

    public FileBrowserItem(Mime mime, String text) {
        this.mime = new SimpleObjectProperty<>(requireNonNull(mime));
        this.text = new SimpleStringProperty(requireNonNull(text));
        this.menuItems = new ArrayList<>();
    }

    public FileBrowserItem(Mime mime, String text, boolean shared) {
        this.mime = new SimpleObjectProperty<>(requireNonNull(mime));
        this.text = new SimpleStringProperty(requireNonNull(text));
        this.menuItems = new ArrayList<>();
    }



    public String getText() {
        return text.get();
    }

    public StringProperty textProperty() {
        return text;
    }

    public void setText(String text) {
        this.text.set(text);
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public Mime getMime() {
        return mime.get();
    }

    public ObjectProperty<Mime> mimeProperty() {
        return mime;
    }

    public void setMime(Mime mime) {
        this.mime.set(mime);
    }

    public void setMenuItems(List<MenuItem> menuItems) {
        this.menuItems = menuItems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileBrowserItem that = (FileBrowserItem) o;
        return getMime().equals(that.getMime()) &&
                getText().equals(that.getText());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMime(), getText());
    }
}
