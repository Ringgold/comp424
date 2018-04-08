package student_player;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import boardgame.Move;
import coordinates.Coord;
import coordinates.Coordinates;
import tablut.TablutBoardState;
import tablut.TablutMove;
import tablut.TablutPlayer;

public class StudentPlayer extends TablutPlayer {
	private int user;
	private int oppo;
	
	//Weights for calculation
	private int Muscov_UserPieceWeight = 18;
	private int Muscov_OppoPieceWeight = 30;
	private int Muscov_SurroundWeight = 20;
	private int Swede_UserPieceWeight = 30;
	private int Swede_OppoPieceWeight = 18;
	private int Swede_SurroundWeight = 20;
	private int KingEscapingWeight = 45;
	
	private int TimeLimit = 1685;

    public StudentPlayer() {
        super("260561361");
    }

    public Move chooseMove(TablutBoardState boardState){
    	// set player and opponent ids
    	if (player_id == TablutBoardState.SWEDE){
    		user = TablutBoardState.SWEDE;
    		oppo = TablutBoardState.MUSCOVITE;
    	} else {
    		user = TablutBoardState.MUSCOVITE;
    		oppo = TablutBoardState.SWEDE;
    	}
    	
    	Node start = new Node(boardState, null);
    	start.buildChildren();
    	List<Node> children = start.getChildren(); 
    	
    	Move winnerMove = getWinnerMove(children);
    	if (winnerMove != null) {
    		return winnerMove;
    	}
    	
    	//try to espace when possible
    	if (user == TablutBoardState.SWEDE) {
    		Move greedyMove = getEscapeMove(start);
        	if (greedyMove != null) {
        		return greedyMove;
        	}
    	}

    	// Monte Carlo Tree Search
    	long startTime = System.currentTimeMillis();
    	while ( (System.currentTimeMillis()-startTime) < TimeLimit){
    		for (Node child : children) {
    			Node promisingNode = getGoodNode(child);
    			TablutBoardState playoutResult = processState(promisingNode.getState());
    			if (playoutResult.getWinner() == user) {
    				child.setValue(child.getValue()+1);
    			}
    		}
        }
        return getBestValue(children).getMove();
    }
    
    private Move getWinnerMove(List<Node> children){
    	for (Node child: children) {
    		TablutBoardState state = child.getState();
    		if (state.getWinner() == user) {
    			return child.getMove();
    		}
    	}
    	return null;
    }
    
    private Move getEscapeMove(Node parent){
    	Move bestMove = null;
    	Coord king = parent.getState().getKingPosition();
    	int minDistance = Coordinates.distanceToClosestCorner(king);
        for (TablutMove move : parent.getState().getLegalMovesForPosition(king)) {
            int moveDistance = Coordinates.distanceToClosestCorner(move.getEndPosition());
            if (moveDistance < minDistance) {
            	TablutBoardState childState = (TablutBoardState) parent.getState().clone();
                childState.processMove(move);
            	if (captureCheck(childState)) {
            		minDistance = moveDistance;
                    bestMove = move;
            	}
            }
        }
        return bestMove;
    }
    
    private boolean captureCheck(TablutBoardState state){
    	int originalNumPieces = state.getNumberPlayerPieces(user);
    	for (TablutMove move: state.getAllLegalMoves()) {
    		TablutBoardState childState = (TablutBoardState) state.clone();
            childState.processMove(move);
    		int newNumPieces = childState.getNumberPlayerPieces(user);
    		if (originalNumPieces-newNumPieces != 0) {
    			return false;
    		}
    	}
    	return true;
    }
    
    private Node getBestValue(List<Node> children){
    	Node highestScore = children.get(0);
    	for (Node child: children) {
    		if (child.getValue() > highestScore.getValue()){
    			highestScore = child;
    		}
    	}
    	return highestScore;
    }
    
    private Node getGoodNode(Node start){
    	start.buildChildren();
    	int bestHeuristic = 0;
    	Node goodNode = null;
    	for (Node child: start.getChildren()){
    		int heuristic = getHeuristicValue(child);
    		if (heuristic > bestHeuristic){
    			goodNode = child;
    		}
    	}
    	return goodNode;
    }
    
    private int getHeuristicValue(Node child){
    	int value = 2000;
    	
    	TablutBoardState currentState = child.getState();
    	if (currentState.gameOver()){
    		if (currentState.getWinner() == user){
    			return 99999;
    		} 
    		if (currentState.getWinner() == oppo){
    			return 0;
    		}
    	}
    	
    	Coord kingPosition = currentState.getKingPosition(); 
    	HashSet<Coord> userPieces = currentState.getPlayerPieceCoordinates();
    	HashSet<Coord> oppoPieces = currentState.getOpponentPieceCoordinates();
    	
    	// gain points for pieces being close to king, lose points for opponent being close to king
    	
    	if (user == TablutBoardState.SWEDE) {
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
    
    private TablutBoardState processState(TablutBoardState state){
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