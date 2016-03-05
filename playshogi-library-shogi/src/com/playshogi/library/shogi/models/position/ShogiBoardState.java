package com.playshogi.library.shogi.models.position;

import com.playshogi.library.models.Square;
import com.playshogi.library.shogi.models.Piece;

public interface ShogiBoardState {

	Piece getPieceAt(int column, int row);

	void setPieceAt(int column, int row, Piece piece);

	void setPieceAt(Square square, Piece piece);

	Piece getPieceAt(Square square);

	int getWidth();

	int getHeight();

	ShogiBoardState opposite();

	boolean hasPlayerPawnOnColumn(boolean isPlayerSente, int column);

}