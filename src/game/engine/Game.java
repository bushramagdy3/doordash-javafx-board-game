package game.engine;

import java.io.IOException;
import java.util.*;

import game.engine.dataloader.DataLoader;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;
import game.engine.monsters.*;
import game.gui.views.BoardView;

public class Game {
	private Board board;
	private ArrayList<Monster> allMonsters;
	private Monster player;
	private Monster opponent;
	private Monster current;
	
	public Monster getCurrent() {
		return current;
	}
	public void setCurrent(Monster current) {
		this.current = current;
	}
	public Board getBoard() {
		return board;
	}
	public ArrayList<Monster> getAllMonsters() {
		return allMonsters;
	}
	public Monster getPlayer() {
		return player;
	}
	public Monster getOpponent() {
		return opponent;
	}
	
	public Game(Role playerRole) throws IOException{
		board = new Board(DataLoader.readCards());
		allMonsters = DataLoader.readMonsters();
		player = selectRandomMonsterByRole(playerRole);
		if(playerRole == Role.LAUGHER)
			opponent = selectRandomMonsterByRole(Role.SCARER);
		else
			opponent = selectRandomMonsterByRole(Role.LAUGHER);
		ArrayList<Monster> stationedMonsters = new ArrayList<Monster>();
		for (Monster m : allMonsters)
		    if (m != player && m != opponent)
		        stationedMonsters.add(m);
		Board.setStationedMonsters(stationedMonsters);
		allMonsters = stationedMonsters;
		board.initializeBoard(DataLoader.readCells());
		current = player;
	}

	private Monster selectRandomMonsterByRole(Role role) {
		Collections.shuffle(allMonsters);
	    return allMonsters.stream()
	    		.filter(m -> m.getRole() == role)
	    		.findFirst()
	    		.orElse(null);
	}
	
	private Monster getCurrentOpponent(){
		if(player == current)
			return opponent;
		else
			return player;
	}
	
	private int rollDice(){
		return ((int) (Math.random()*6) ) + 1;
	}
	
	public int playTurn() throws InvalidMoveException {

	    int roll = rollDice();

	    if(current.isFrozen())
	        current.setFrozen(false);

	    else
	        board.moveMonster(current, roll, getCurrentOpponent());

	    switchTurn();

	    return roll;
	}
	
	private void switchTurn(){
		current = getCurrentOpponent();
	}
	
	public void usePowerup() throws OutOfEnergyException {

	    if (current.getEnergy() < Constants.POWERUP_COST)
	        throw new OutOfEnergyException();

	    current.setEnergy(current.getEnergy() - Constants.POWERUP_COST);

	    current.executePowerupEffect(getCurrentOpponent());
	}
	
	private boolean checkWinCondition(Monster monster) {

	    return monster.getPosition() == Constants.WINNING_POSITION &&
	           monster.getEnergy() >= Constants.WINNING_ENERGY;
	}
	
	public Monster getWinner() {

	    if (checkWinCondition(player))
	        return player;

	    if (checkWinCondition(opponent))
	        return opponent;

	    return null;
	}
	
	
}
