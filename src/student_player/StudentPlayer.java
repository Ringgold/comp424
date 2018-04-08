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
	
	private int TimeLimit = 1485;

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
    	
    	Move winnerMove = MyTools.getWinnerMove(children, user);
    	if (winnerMove != null) {
    		return winnerMove;
    	}
    	
    	//try to espace when possible
    	if (user == TablutBoardState.SWEDE) {
    		Move greedyMove = MyTools.getEscapeMove(start, user);
        	if (greedyMove != null) {
        		return greedyMove;
        	}
    	}

    	// Monte Carlo Tree Search
    	long startTime = System.currentTimeMillis();
    	while ( (System.currentTimeMillis()-startTime) < TimeLimit){
    		for (Node child : children) {
    			Node promisingNode = MyTools.getGoodNode(child, user, oppo);
    			TablutBoardState playoutResult = MyTools.processState(promisingNode.getState());
    			if (playoutResult.getWinner() == user) {
    				child.setValue(child.getValue()+1);
    			}
    		}
        }
        return MyTools.getBestValue(children).getMove();
    }
}