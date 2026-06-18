package game.engine.exceptions;

@SuppressWarnings("serial")
public abstract class GameActionException extends Exception{
	public GameActionException(){
	}
	public GameActionException(String message){
		super(message);
	}
}
