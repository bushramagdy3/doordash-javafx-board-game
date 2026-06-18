package game.engine.monsters;

import game.engine.*;

public abstract class Monster implements Comparable<Monster> {
	private String name;
	private String description;
	private Role originalRole;
	
	private Role role;
	private int energy;
	private int position;
	private boolean frozen;
	private boolean shielded;
	private int confusionTurns;

	
	public Monster(String name, String description, Role originalRole, int energy){
		this.name = name;
		this.description = description;
		this.originalRole = originalRole;
		this.energy = energy;
		this.role = originalRole;
		this.position = 0;
		this.confusionTurns = 0;
		this.frozen = false;
		this.shielded = false;
		
	}


	public String getName() {
		return name;
	}


	public String getDescription() {
		return description;
	}


	public int getEnergy() {
		return energy;
	}	


	public int getPosition() {
		return position;
	}


	public boolean isFrozen() {
		return frozen;
	}

	public boolean isShielded() {
		return shielded;
	}


	public int getConfusionTurns() {
		return confusionTurns;
	}

	public Role getRole(){
		return role;
	}

	public Role getOriginalRole(){
		return originalRole;
	}

	public void setEnergy(int energy) {
		this.energy = Math.max(Constants.MIN_ENERGY, energy);
	}

	public final void cheatAddEnergy(int amount) {
		this.energy = Math.max(Constants.MIN_ENERGY, this.energy + amount);
	}


	public void setPosition(int position) {
		this.position = position % Constants.BOARD_SIZE;
	}


	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}

	public void setShielded(boolean shielded) {
		this.shielded = shielded;
	}


	public void setConfusionTurns(int confusionTurns) {
		this.confusionTurns = confusionTurns;
	}

	public void setRole(Role role){
		this.role = role;
	}

	public int compareTo(Monster o){
		return this.position - o.position;
	}

	public abstract void executePowerupEffect(Monster opponentMonster);

	public boolean isConfused() {
		return confusionTurns > 0;
	}
	public void move(int distance) {
	    int newPosition = (position + distance) % Constants.BOARD_SIZE;

	    if (newPosition < 0)
	        newPosition += Constants.BOARD_SIZE;

	    position = newPosition;
	}
	
	public final void alterEnergy(int energy) {
	    if (isShielded() && energy < 0) {
	        setShielded(false);
	    } else {
	        setEnergy(getEnergy() + energy);
	    }
	}
	
	public void decrementConfusion() {

	    if (confusionTurns > 0) {
	        confusionTurns--;

	        if (confusionTurns == 0) {
	            role = originalRole;
	        }
	    }
	}



}
