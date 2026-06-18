package game.engine.cells;
import game.engine.Board;
import game.engine.monsters.Monster;

public class CardCell extends Cell{
	public CardCell(String name){
		super(name);
	}
	
	@Override
	public void onLand(Monster landingMonster, Monster opponentMonster) {
	    super.onLand(landingMonster, opponentMonster);
	    Board.drawCard().performAction(landingMonster, opponentMonster);
	}
}