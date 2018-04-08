package student_player;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import boardgame.Move;
import coordinates.Coord;
import coordinates.Coordinates;
import tablut.TablutBoardState;
import tablut.TablutMove;

public class MyTools {
	//Weights for calculation
	private static int Muscov_UserPieceWeight = 18;
	private static int Muscov_OppoPieceWeight = 30;
	private static int Muscov_SurroundWeight = 20;
	private static int Swede_UserPieceWeight = 30;
	private static int Swede_OppoPieceWeight = 18;
	private static int Swede_SurroundWeight = 20;
	private static int KingEscapingWeight = 45;
	
    public static double getSomething() {
        return Math.random();
    }
    public static Move getWinnerMove(List<Node> children, int userID){
    	for (Node child: children) {
    		TablutBoardState state = child.getState();
    		if (state.getWinner() == userID) {
    			return child.getMove();
    		}
    	}
    	return null;
    }
    
    public static Move getEscapeMove(Node parent, int userID){
    	Move bestMove = null;
    	Coord king = parent.getState().getKingPosition();
    	int minDistance = Coordinates.distanceToClosestCorner(king);
        for (TablutMove move : parent.getState().getLegalMovesForPosition(king)) {
            int moveDistance = Coordinates.distanceToClosestCorner(move.getEndPosition());
            if (moveDistance < minDistance) {
            	TablutBoardState childState = (TablutBoardState) parent.getState().clone();
                childState.processMove(move);
            	if (captureCheck(childState, userID)) {
            		minDistance = moveDistance;
                    bestMove = move;
            	}
            }
        }
        return bestMove;
    }
    
    public static boolean captureCheck(TablutBoardState state, int userID){
    	int originalNumPieces = state.getNumberPlayerPieces(userID);
    	for (TablutMove move: state.getAllLegalMoves()) {
    		TablutBoardState childState = (TablutBoardState) state.clone();
            childState.processMove(move);
    		int newNumPieces = childState.getNumberPlayerPieces(userID);
    		if (originalNumPieces-newNumPieces != 0) {
    			return false;
    		}
    	}
    	return true;
    }
    
    public static Node getBestValue(List<Node> children){
    	Node highestScore = children.get(0);
    	for (Node child: children) {
    		if (child.getValue() > highestScore.getValue()){
    			highestScore = child;
    		}
    	}
    	return highestScore;
    }
    
    public static Node getGoodNode(Node start, int userID, int oppoID){
    	start.buildChildren();
    	int bestHeuristic = 0;
    	Node goodNode = null;
    	for (Node child: start.getChildren()){
    		int heuristic = getHeuristicValue(child, userID, oppoID);
    		if (heuristic > bestHeuristic){
    			goodNode = child;
    		}
    	}
    	return goodNode;
    }
    
    public static int getHeuristicValue(Node child, int userID, int oppoID){
    	int value = 2000;
    	
    	TablutBoardState currentState = child.getState();
    	if (currentState.gameOver()){
    		if (currentState.getWinner() == userID){
    			return 99999;
    		} 
    		if (currentState.getWinner() == oppoID){
    			return 0;
    		}
    	}
    	
    	Coord kingPosition = currentState.getKingPosition(); 
    	HashSet<Coord> userPieces = currentState.getPlayerPieceCoordinates();
    	HashSet<Coord> oppoPieces = currentState.getOpponentPieceCoordinates();
    	
    	// gain points for pieces being close to king, lose points for opponent being close to king
    	
    	if (userID == TablutBoardState.SWEDE) {
    		for (Coord coord: currentState.getPlayerPieceCoordinates()) {
        		value = value - Swede_SurroundWeight * coord.distance(kingPosition);
    		}
    		for (Coord coord: currentState.getOpponentPieceCoordinates()) {
    			value = value + Swede_SurroundWeight * coord.distance(kingPosition);
    		}
    		value = value + Swede_UserPieceWeight * userPieces.size() -  Swede_OppoPieceWeight * oppoPieces.size();
    		value -= KingEscapingWeight * Coordinates.distanceToClosestCorner(kingPosition);
    	} else {
    		for (Coord coord: currentState.getPlayerPieceCoordinates()) {
        		value = value - Muscov_SurroundWeight * coord.distance(kingPosition);
    		}
    		for (Coord coord: currentState.getOpponentPieceCoordinates()) {
    			value = value + Muscov_SurroundWeight * coord.distance(kingPosition);
    		}
    		value = value + Muscov_UserPieceWeight * userPieces.size() -  Muscov_OppoPieceWeight * oppoPieces.size();
    		value += KingEscapingWeight * Coordinates.distanceToClosestCorner(kingPosition);
    	}
    	return value;
    }
    
    public static TablutBoardState processState(TablutBoardState state){
    	TablutBoardState tempState = (TablutBoardState) state.clone();
        if (tempState.gameOver()){
            return state;
        }
        while (!tempState.gameOver()){
        	Random rand = new Random();
        	List<TablutMove> moves = tempState.getAllLegalMoves();
        	tempState.processMove(moves.get(rand.nextInt(moves.size())));
        }
        return tempState;
    }
}
