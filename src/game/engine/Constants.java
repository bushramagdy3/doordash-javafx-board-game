package game.engine;

public final class Constants {
	//Board Constants: 
	public static final int BOARD_SIZE = 100;
	public static final int BOARD_ROWS = 10;
	public static final int BOARD_COLS = 10;
	public static final int WINNING_POSITION = 99;
	public static final int STARTING_POSITION = 0;
	//Special Cells Positions Constants:
	public static final int[] MONSTER_CELL_INDICES = {2,18,34,54,82,88}; 
	public static final int[] CONVEYOR_CELL_INDICES = {6,22,44,52,66}; //(Start of the belt)
	public static final int[] SOCK_CELL_INDICES = {32, 42, 74, 84, 98}; //(Start of the sock)
	public static final int[] CARD_CELL_INDICES = {4, 12, 28, 36, 48, 56, 60, 76, 86, 90};
	//Energy Constants:
	public static final int WINNING_ENERGY = 1000;
	public static final int MIN_ENERGY = 0;
	//Monster Constants:
	public static final int MULTITASKER_BONUS = 200;
	public static final int SCHEMER_STEAL = 10;
	//Cell Constants:
	public static final int SLIP_PENALTY = 100;
	//Power Constants: 
	public static final int POWERUP_COST = 500;
}
