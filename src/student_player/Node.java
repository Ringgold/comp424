package student_player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import boardgame.Move;
import tablut.TablutBoardState;
import tablut.TablutMove;

public class Node {
	TablutBoardState state;
	Move move;
    List<Node> children;
    int value = 0;
    
    public Node(TablutBoardState state, Move move){
    	this.state = state;
    	this.move = move;
    }
    
    public void buildChildren(){
    	children = new ArrayList<Node>();
    	List<TablutMove> allMoves = state.getAllLegalMoves();
    	for (TablutMove move : allMoves) {
            TablutBoardState childState = (TablutBoardState) state.clone();
            childState.processMove(move);
            Node childNode = new Node(childState, move);
            children.add(childNode);
        }
    }
    
    //All basic methods
    public List<Node> getChildren(){
    	return children;
    }
    public TablutBoardState getState(){
    	return state;
    }
    public int getValue(){
    	return value;
    }
    public Move getMove(){
    	return move;
    }
    public void setValue(int value){
    	this.value = value;
    }

    //Pick random child
    public Node getRandomChild(){
    	Random rand = new Random();
    	int random = rand.nextInt(children.size()-1);
    	return children.get(random);
    }

}