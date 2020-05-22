/*
 * The MIT License
 *
 * Copyright (c) 2020 Nefele <https://github.com/nefele-org>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.nefele.ui.controls;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.shape.Rectangle;
import org.nefele.Application;
import org.nefele.Resources;
import org.nefele.core.Mime;
import org.nefele.fs.MergeFileSystem;
import org.nefele.fs.MergePath;
import org.nefele.ui.Themeable;

import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.requireNonNull;


public class FileBrowser extends ScrollPane implements Initializable, Themeable {

    private final ObjectProperty<Path> currentPath;

    private final ObservableList<FileBrowserItem> items;
    private final ObjectProperty<FileBrowserItem> selectedItem;
    private final HashSet<FileBrowserItem> selectedItems;

    private final ObservableList<FileBrowserCell> cells;
    private final ObservableList<FileBrowserCell> selectedCells;

    private FileBrowserItemFactory itemFactory;
    private final ExecutorService executorService;

    private final Stack<URI> historyBack;
    private final ArrayDeque<URI> historyForward;

    private Instant lastMousePressed;
    private final ContextMenu contextMenu;

    private Rectangle selectionRectangle;
    private Rectangle2D selectionAnchor;

    @FXML private FlowPane cellPane;
    @FXML private BorderPane selectionPane;



    public FileBrowser() {

        this.currentPath = new SimpleObjectProperty<>(null);
        this.items = FXCollections.observableArrayList();
        this.cells = FXCollections.observableArrayList();
        this.selectedCells = FXCollections.observableArrayList();
        this.executorService = Executors.newSingleThreadExecutor();

        this.selectedItems = new HashSet<>();
        this.selectedItem = new SimpleObjectProperty<>(null);

        this.historyBack = new Stack<>();
        this.historyForward = new ArrayDeque<>();

        this.lastMousePressed = Instant.now();
        this.contextMenu = new ContextMenu();


        Resources.getFXML(this, "/fxml/controls/FileBrowser.fxml");
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        currentPath.addListener((v, o, n) -> {

            if(o != null)
                historyBack.push(o.toUri());

            setItems(requireNonNull(itemFactory).call(n));

        });


        items.addListener((ListChangeListener<FileBrowserItem>) change -> {

            while(change.next()) {

                if(change.wasRemoved()) {
                    executorService.submit(() -> {
                        change.getRemoved().forEach(i -> {
                            Platform.runLater(() -> cells.removeIf(j -> i.equals(j.getItem())));
                        });
                    });
                }




                if(change.wasAdded()) {

                    executorService.submit(() -> {
                        change.getAddedSubList().forEach(i -> {

                            final CountDownLatch countDownLatch = new CountDownLatch(1);

                            Platform.runLater(() -> {
                                cells.add(new FileBrowserCell(i));
                                countDownLatch.countDown();
                            });

                            try {
                                countDownLatch.await();
                            } catch (InterruptedException ignored) { }

                        });

                    });

                }

            }

        });


        cells.addListener((ListChangeListener<FileBrowserCell>) change -> {

            while(change.next()) {

                if(change.wasRemoved())
                    change.getRemoved().forEach(cellPane.getChildren()::remove);

                if(change.wasAdded())
                    change.getAddedSubList().forEach(cellPane.getChildren()::add);

            }

        });


        selectedCells.addListener((ListChangeListener<FileBrowserCell>) change -> {

            while(change.next()) {

                if(change.wasRemoved()) {
                    change.getRemoved().forEach(i -> i.selectedProperty().set(false));
                    change.getRemoved().forEach(i -> selectedItems.remove(i.getItem()));
                }

                if(change.wasAdded()) {
                    change.getAddedSubList().forEach(i -> i.selectedProperty().set(true));
                    change.getAddedSubList().forEach(i -> selectedItems.add(i.getItem()));
                }

            }

        });




        cellPane.setOnMousePressed(this::onMousePressed);
        cellPane.setOnMouseDragged(this::onMouseDragged);
        cellPane.setOnMouseReleased(this::onMouseReleased);
        cellPane.requestLayout();

        Application.getInstance().getViews().add(this);

    }

    @Override
    public void initializeInterface() {
        Resources.getCSS(this, "/css/filebrowser-base.css");
        Resources.getCSS(this, "/css/filebrowser-cell.css");
    }





    public Path getCurrentPath() {
        return currentPath.get();
    }

    public ObjectProperty<Path> currentPathProperty() {
        return currentPath;
    }

    public ObservableList<FileBrowserItem> getItems() {
        return items;
    }

    public void setCurrentPath(Path currentPath) {
        this.currentPath.set(currentPath);
    }

    public void setItems(List<FileBrowserItem> items) {
        this.items.clear();
        this.items.addAll(items);
    }

    public FileBrowserItemFactory getItemFactory() {
        return itemFactory;
    }

    public void setItemFactory(FileBrowserItemFactory itemFactory) {
        this.itemFactory = itemFactory;
    }

    public void browseHistory(int historyAdvance) {


            URI cwd = null;

            if (historyAdvance > 0) {
                if(historyForward.size() > 0) {
                    historyBack.push(getCurrentPath().toUri());
                    cwd = historyForward.pop();
                }
            } else {
                if(historyBack.size() > 0) {
                    historyForward.push(getCurrentPath().toUri());
                    cwd = historyBack.pop();
                }
            }

            if(cwd != null) {
                setCurrentPath(Path.of(cwd));
                historyBack.pop();
            }

    }

    public void update() {
        setItems(requireNonNull(getItemFactory().call(currentPath.get())));
    }


    private void onMousePressed(MouseEvent e) {

        Duration mouseDelta = Duration.between(lastMousePressed, Instant.now());
        lastMousePressed = Instant.now();


        AtomicBoolean found = new AtomicBoolean(false);

        cells.forEach(i -> {

            Bounds b = i.getBoundsInParent();

            if (e.getX() > b.getMinX() && e.getX() < b.getMaxX()
                    && e.getY() > b.getMinY() && e.getY() < b.getMaxY()) {

                if (mouseDelta.toMillis() > 300) {

                    if (!selectedCells.contains(i)) {

                        if (!e.isControlDown() && !e.isShiftDown())
                            selectedCells.clear();

                        if (e.isShiftDown()) {

                            if (selectedCells.size() > 0) {

                                int min = selectedCells.stream().mapToInt(cells::indexOf).min().orElse(-1);
                                int max = selectedCells.stream().mapToInt(cells::indexOf).max().orElse(-1);

                                min = Math.min(min, cells.indexOf(i));
                                max = Math.max(max, cells.indexOf(i));

                                if (min != max)
                                    selectedCells.addAll(cells.subList(min, max));

                            }

                        }


                        selectedCells.add(i);

                    } else {

                        if (e.isPrimaryButtonDown()) {

                            if (e.isControlDown() || e.isShiftDown())
                                selectedCells.remove(i);
                            else
                                selectedCells.removeIf(j -> !j.equals(i));

                        }

                    }


                    if (e.isSecondaryButtonDown()) {

                        selectedItem.set(i.getItem());

                        contextMenu.getItems().clear();
                        contextMenu.getItems().addAll(i.getItem().getMenuItems());
                        contextMenu.show(this, e.getScreenX(), e.getScreenY());

                    } else
                        contextMenu.hide();


                } else {

                    if (e.isPrimaryButtonDown()) {

                        selectedItem.set(i.getItem());

                        if(!i.getItem().getMenuItems().isEmpty())
                            i.getItem().getMenuItems().get(0).fire();

                    }

                }


                found.set(true);
            }

        });



        if(!found.get()) {

            if(!e.isShiftDown() && !e.isControlDown())
                selectedCells.clear();

            contextMenu.hide();


            selectionAnchor = new Rectangle2D(e.getX(), e.getY(), 0, 0);
            selectionRectangle = new Rectangle(e.getX(), e.getY(), 0, 0);
            selectionRectangle.getStyleClass().add("filebrowser-selection-rectangle");

            selectionPane.getChildren().add(selectionRectangle);

        }

    }

    private void onMouseDragged(MouseEvent e) {

        if(selectionRectangle != null) {

            if (e.getX() < selectionAnchor.getMinX()) {
                selectionRectangle.setWidth(selectionAnchor.getMinX() - e.getX());
                selectionRectangle.setX(e.getX());
            } else
                selectionRectangle.setWidth(e.getX() - selectionAnchor.getMinX());

            if (e.getY() < selectionAnchor.getMinY()) {
                selectionRectangle.setHeight(selectionAnchor.getMinY() - e.getY());
                selectionRectangle.setY(e.getY());
            } else
                selectionRectangle.setHeight(e.getY() - selectionAnchor.getMinY());


            cells.forEach(i -> {

                Bounds b = i.getBoundsInParent();

                if(Math.max(selectionRectangle.getX(), b.getMinX()) < Math.min(selectionRectangle.getX() + selectionRectangle.getWidth(), b.getMaxX())
                && Math.max(selectionRectangle.getY(), b.getMinY()) < Math.min(selectionRectangle.getY() + selectionRectangle.getHeight(), b.getMaxY()))
                    selectedCells.add(i);
                else
                    selectedCells.remove(i);

            });

        }

    }

    private void onMouseReleased(MouseEvent e) {

        if(selectionRectangle != null) {

            selectionPane.getChildren().remove(selectionRectangle);
            selectionRectangle = null;

        }

    }


    public HashSet<FileBrowserItem> getSelectedItems() {
        return selectedItems;
    }

    public FileBrowserItem getSelectedItem() {
        return selectedItem.get();
    }

    public void browse(Path path) {
        historyForward.clear();
        setCurrentPath(path);
    }

}
