package com.playshogi.website.gwt.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;
import com.playshogi.website.gwt.client.events.ListCollectionGamesEvent;
import com.playshogi.website.gwt.client.events.ListGameCollectionsEvent;
import com.playshogi.website.gwt.client.place.GameCollectionsPlace;
import com.playshogi.website.gwt.client.place.ViewKifuPlace;
import com.playshogi.website.gwt.shared.models.GameCollectionDetails;
import com.playshogi.website.gwt.shared.models.KifuDetails;

import java.util.Arrays;

@Singleton
public class GameCollectionsView extends Composite {

    private final PlaceController placeController;

    interface MyEventBinder extends EventBinder<GameCollectionsView> {
    }

    private final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);

    private final CellTable<GameCollectionDetails> collectionsTable;
    private final CellTable<KifuDetails> kifusTable;

    @Inject
    public GameCollectionsView(final PlaceController placeController) {
        this.placeController = placeController;
        GWT.log("Creating game collections view");
        FlowPanel flowPanel = new FlowPanel();

        flowPanel.add(new HTML(SafeHtmlUtils.fromSafeConstant("Game collections<br/>")));

        collectionsTable = createGameCollectionTable();
        kifusTable = createKifusTable();

        flowPanel.add(collectionsTable);
        flowPanel.add(kifusTable);

        initWidget(flowPanel);
    }

    private CellTable<GameCollectionDetails> createGameCollectionTable() {
        final CellTable<GameCollectionDetails> collectionsTable;
        collectionsTable = new CellTable<>();
        collectionsTable.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.ENABLED);

        TextColumn<GameCollectionDetails> idColumn = new TextColumn<GameCollectionDetails>() {
            @Override
            public String getValue(final GameCollectionDetails object) {
                return String.valueOf(object.getId());
            }
        };
        collectionsTable.addColumn(idColumn, "Id");

        TextColumn<GameCollectionDetails> nameColumn = new TextColumn<GameCollectionDetails>() {
            @Override
            public String getValue(final GameCollectionDetails object) {
                return String.valueOf(object.getName());
            }
        };
        collectionsTable.addColumn(nameColumn, "Name");

        final SingleSelectionModel<GameCollectionDetails> selectionModel = new SingleSelectionModel<>();
        collectionsTable.setSelectionModel(selectionModel);
        selectionModel.addSelectionChangeHandler(event -> {
            GameCollectionDetails selected = selectionModel.getSelectedObject();
            if (selected != null) {
                GWT.log("Going to game collection " + selected.getId());
                placeController.goTo(new GameCollectionsPlace(selected.getId()));
            }
        });
        return collectionsTable;
    }

    private CellTable<KifuDetails> createKifusTable() {
        final CellTable<KifuDetails> collectionsTable;
        collectionsTable = new CellTable<>();
        collectionsTable.setKeyboardSelectionPolicy(HasKeyboardSelectionPolicy.KeyboardSelectionPolicy.ENABLED);

        collectionsTable.addColumn(new TextColumn<KifuDetails>() {
            @Override
            public String getValue(final KifuDetails object) {
                return String.valueOf(object.getId());
            }
        }, "Id");

        collectionsTable.addColumn(new TextColumn<KifuDetails>() {
            @Override
            public String getValue(final KifuDetails object) {
                return String.valueOf(object.getSente());
            }
        }, "Sente");

        collectionsTable.addColumn(new TextColumn<KifuDetails>() {
            @Override
            public String getValue(final KifuDetails object) {
                return String.valueOf(object.getGote());
            }
        }, "Gote");

        final SingleSelectionModel<KifuDetails> selectionModel = new SingleSelectionModel<>();
        collectionsTable.setSelectionModel(selectionModel);
        selectionModel.addSelectionChangeHandler(event -> {
            KifuDetails selected = selectionModel.getSelectedObject();
            if (selected != null) {
                GWT.log("Going to kifu" + selected.getId());
                placeController.goTo(new ViewKifuPlace(selected.getId()));
            }
        });
        return collectionsTable;
    }

    public void activate(final EventBus eventBus) {
        GWT.log("Activating game collections view");
        eventBinder.bindEventHandlers(this, eventBus);
        collectionsTable.setVisible(false);
        kifusTable.setVisible(false);
    }

    @EventHandler
    public void onListGameCollectionsEvent(final ListGameCollectionsEvent event) {
        GWT.log("GameCollectionsView: handle GameCollectionsEvent");
        collectionsTable.setRowCount(event.getDetails().length);
        collectionsTable.setRowData(0, Arrays.asList(event.getDetails()));
        collectionsTable.setVisible(true);
    }

    @EventHandler
    public void onListCollectionGamesEvent(final ListCollectionGamesEvent event) {
        GWT.log("GameCollectionsView: handle GameCollectionsEvent");
        kifusTable.setRowCount(event.getDetails().length);
        kifusTable.setRowData(0, Arrays.asList(event.getDetails()));
        kifusTable.setVisible(true);
    }
}