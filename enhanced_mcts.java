import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class enhanced_mcts {
	
	final static double STARTING_BRANCHING_FACTOR = 20;
	final static double LOOP_BRANCHING_FACTOR = 10;
	final static double bf_constant = 1;
	public static double TIME_PER_MOVE_MILLI = 10 * Math.pow(10, 3);
	final static int MAX_VALUE = 1000000;
	final static double TUNABLE_CONSTANT = 2;

	public static void main(String[] args) {
		if (args.length > 0) {
			/*
			 *  For test script
			 *  args[1] = W/B for non-random computer agent
			 *  args[2] = time per move (in seconds)
			 *  args[3] = num of games to play
			 *  Output is the win percentage for the non-random agent
			 */
			int player = 2;
			char p1_color = 'B';
			char p2_color = 'W';
			if (args[1].equals("W")) {
				player = 1;
				p1_color = 'W';
				p2_color = 'B';
			}
			
			TIME_PER_MOVE_MILLI = Double.parseDouble(args[2]) * Math.pow(10, 3);
			int num_games = Integer.parseInt(args[3]);
			
			double num_won = 0;
			
			if (args[0].equals("random")) {
				for (int i = 0; i < num_games; i++) {
					num_won += computer_play_random(null, player, true);
				}
				
				System.out.println("Win percentage for agent (" + p1_color + ") vs random player (" + p2_color + ") with " + args[2] + " seconds per move over " + args[3] + " games: " + (num_won / num_games));
			} else if (args[0].equals("minimax")) {
				for (int i = 0; i < num_games; i++) {
					num_won += computer_play_minimax(player);
				}
				
				System.out.println("Win percentage for agent (" + p1_color + ") vs depth-2 minimax player (" + p2_color + ") with " + args[2] + " seconds per move over " + args[3] + " games: " + (num_won / num_games));
			} else if (args[0].equals("mcts")) {
				for (int i = 0; i < num_games; i++) {
					num_won += computer_play_mcts(player);
				}
				
				System.out.println("Win percentage for agent (" + p1_color + ") vs mcts player (" + p2_color + ") with " + args[2] + " seconds per move over " + args[3] + " games: " + (num_won / num_games));
			} else if (args[0].equals("time")) {
				// args[4] = time for worse player
				double worse_time = Double.parseDouble(args[4]) * Math.pow(10, 3);
				
				for (int i = 0; i < num_games; i++) {
					num_won += computer_play_mcts_less_time(player, worse_time);
				}
				
				System.out.println("Win percentage for agent (" + p1_color + ") vs same minimax+mcts agent with less thinking time (" + p2_color + ") with " + args[2] + " and " + args[4] + " seconds per move, respectively, over " + args[3] + " games: " + (num_won / num_games));
			} else {
				System.out.println("Invalid args.");
			}
		} else {
			// interactive version
			System.out.println("Enter one of: \"two player\", \"play computer\", \"computer plays random\"");
			
			Scanner sc = new Scanner(System.in);
			String input = sc.nextLine();
			if (input.equals("two player")) {
				play_game(sc);
			} else if (input.equals("play computer")) {
				play_computer(sc, get_player(sc));
			} else if (input.equals("computer plays random")) {
				computer_play_random(sc, get_player(sc), false);
			}
			sc.close();
		}
	}
	
	public static int get_player(Scanner sc) {
		System.out.println("Enter \"W\" for white and \"B\" for black.");
		if (sc.nextLine().equals("W")) {
			return 1;
		}
		return 2;
	}
	
	public static double computer_play_mcts_less_time(int player, double worse_time) {
		Pentago game = new Pentago();
		Pentago.Position pos = game.get_initial_pos();
		
		double save = TIME_PER_MOVE_MILLI;
		
		while (pos.is_terminal() == 0) {
			int[] move;
			if (pos.get_next_player() == player) {
				// computer's turn
				move = get_move_minimax_mcts(pos);
			} else {
				// minimax agent's turn
				TIME_PER_MOVE_MILLI = worse_time;
				move = get_move_minimax_mcts(pos);
				TIME_PER_MOVE_MILLI = save;
			}
			
			pos = pos.make_move(move);
		}
		
		if (pos.is_terminal() == player) {
			return 1;
		} else if (pos.is_terminal() == 3) {
			return .5;
		}
		return 0;
	}
	
	public static double computer_play_mcts(int player) {
		Pentago game = new Pentago();
		Pentago.Position pos = game.get_initial_pos();
		
		while (pos.is_terminal() == 0) {
			int[] move;
			if (pos.get_next_player() == player) {
				// computer's turn
				move = get_move_minimax_mcts(pos);
			} else {
				// minimax agent's turn
				move = get_move_basic_mcts(pos);
			}
			
			pos = pos.make_move(move);
		}
		
		if (pos.is_terminal() == player) {
			return 1;
		} else if (pos.is_terminal() == 3) {
			return .5;
		}
		return 0;
	}
	
	public static double computer_play_minimax(int player) {
		Pentago game = new Pentago();
		Pentago.Position pos = game.get_initial_pos();
		
		while (pos.is_terminal() == 0) {
			int[] move;
			if (pos.get_next_player() == player) {
				// computer's turn
				move = get_move_minimax_mcts(pos);
			} else {
				// minimax agent's turn
				List<int[]> starting_moves = get_best_moves_with_minimax(pos);
				move = starting_moves.get(0);
			}
			
			pos = pos.make_move(move);
		}
		
		if (pos.is_terminal() == player) {
			return 1;
		} else if (pos.is_terminal() == 3) {
			return .5;
		}
		return 0;
	}
	
	public static double computer_play_random(Scanner sc, int player, boolean script) {	
		if (!script) {
			get_time(sc);
		}
		
		Pentago game = new Pentago();
		Pentago.Position pos = game.get_initial_pos();
		if (!script) {
			pos.display_position();
		}
		
		while (pos.is_terminal() == 0) {
			int[] move;
			if (pos.get_next_player() == player) {
				// computer's turn
				move = get_move_minimax_mcts(pos);
			} else {
				// random agent's turn
				List<int[]> moves = pos.get_legal_moves();
				move = moves.get((int) (Math.random() * moves.size()));
			}
			
			pos = pos.make_move(move);
			if (!script) {
				pos.display_position();
			}
		}
		
		if (script) {
			if (pos.is_terminal() == player) {
				return 1;
			} else if (pos.is_terminal() == 3) {
				return .5;
			}
		} else {
			display_result(pos.is_terminal(), player);
		}
		return 0;
	}
	
	public static void play_game(Scanner sc) {
		System.out.println("\nEnter \"STOP\" to stop the game.");
		System.out.println("Moves are given as 4-tuples, where a move \"r c q d\" means place a marble in row r, column c, and rotate quadrant q in the direction d.");
		System.out.println("The quadrants are {0, 1, 2, 3} going CCW from the top-left quadrant, and d=0 is CCW and d=1 is CW.\n");
		
		Pentago game = new Pentago();
		Pentago.Position pos = game.get_initial_pos();
		pos.display_position();
		
		while (true) {
			String line = sc.nextLine();
			if (line.equals("STOP")) {
				return;
			}
			
			String[] move_str = line.split(" ");
			int[] move_int = new int[4];
			for (int i = 0; i < 4; i++) {
				move_int[i] = Integer.parseInt(move_str[i]);
			}
			pos = pos.make_move(move_int);
			pos.display_position();
			
			int winner = pos.is_terminal();
			
			if (winner == 1) {
				System.out.println("White wins!");
			} else if (winner == 2) {
				System.out.println("Black wins!");
			} else if (winner == 3) {
				System.out.println("Draw!");
			}
		}
	}
	
	public static void play_computer(Scanner sc, int player) {
		get_time(sc);
		System.out.println("\nEnter \"STOP\" to stop the game.");
		
		Pentago game = new Pentago();
		Pentago.Position pos = game.get_initial_pos();
		pos.display_position();
		
		while (pos.is_terminal() == 0) {
			int[] move;
			if (pos.get_next_player() == player) {
				// player's turn
				move = new int[4];
				
				// get the player's move
				while (true) {
					String line = sc.nextLine();
					if (line.equals("STOP")) {
						return;
					}
					
					String[] move_str = line.split(" ");
					for (int i = 0; i < 4; i++) {
						move[i] = Integer.parseInt(move_str[i]);
					}
					
					if (pos.is_valid_move(move)) {
						break;
					} else {
						System.out.println("Entered an invalid move.");
					}
				}
			} else {
				// computer's turn
				System.out.println("Computer move...");
				move = get_move_minimax_mcts(pos);
			}
			
			pos = pos.make_move(move);
			pos.display_position();
		}
		
		display_result(pos.is_terminal(), player);
	}
	
	public static void get_time(Scanner sc) {
		System.out.println("Enter the time (in seconds) the computer has for each move (default is 10 seconds).");
		String time_line = sc.nextLine();
		if (time_line.length() > 0) {
			TIME_PER_MOVE_MILLI = Double.parseDouble(time_line) * Math.pow(10, 3);
		}
	}
	
	public static void display_result(int winner, int player) {
		if (winner == 3) {
			System.out.println("Draw!");
		} else if (winner == player) {
			System.out.println("You win!");
		} else {
			System.out.println("Computer wins!");
		}
	}

	public static int[] get_move_basic_mcts(Pentago.Position pos) {
		long start_time = System.currentTimeMillis();
		int player = pos.get_next_player();
		
		HashMap<String, double[]> tt = new HashMap<String, double[]>();
		tt.put(pos.convert_string(), new double[2]);
		
		// run for at most TIME_PER_MOVE_MILLI seconds
		while (System.currentTimeMillis() - start_time <= TIME_PER_MOVE_MILLI) {
			Pentago.Position cur_pos = pos;
			ArrayList<Pentago.Position> visited_pos = new ArrayList<Pentago.Position>();
			visited_pos.add(pos);
			
			while (!basic_is_leaf_or_terminal(cur_pos, tt)) {
				boolean max_pos = true;
				if (cur_pos.get_next_player() != player) {
					max_pos = false;
				}
				int[] best_move = get_best_move(cur_pos, cur_pos.get_legal_moves(), max_pos, tt);
				cur_pos = cur_pos.make_move(best_move);
				visited_pos.add(cur_pos);
			}
			
			if (cur_pos.is_terminal() != 0) {
				backtracking(tt, visited_pos, player, cur_pos.is_terminal());
			} else {
				if (!tt.containsKey(cur_pos.convert_string())) {
					tt.put(cur_pos.convert_string(), new double[2]);
				}
				
				List<int[]> child_moves = cur_pos.get_legal_moves();
				
				// initialize children
				List<Pentago.Position> uninitialized_children = new ArrayList<Pentago.Position>();
				for (int[] child_move : child_moves) {
					Pentago.Position child_pos = cur_pos.make_move(child_move);
					if (!tt.containsKey(child_pos.convert_string())) {
						tt.put(child_pos.convert_string(), new double[2]);
						uninitialized_children.add(child_pos);
					}
				}
				
				Pentago.Position playout_pos;
				if (uninitialized_children.size() > 0) {
					playout_pos = uninitialized_children.get((int) (Math.random() * uninitialized_children.size()));
				} else {
					int[] random_move = child_moves.get((int) (Math.random() * child_moves.size()));
					playout_pos = cur_pos.make_move(random_move);
				}
				visited_pos.add(playout_pos);
				
				int winner = playout(playout_pos);
				backtracking(tt, visited_pos, player, winner);
			}
		}

		return get_best_starting_move(pos, pos.get_legal_moves(), tt);
	}

	public static boolean basic_is_leaf_or_terminal(Pentago.Position cur_pos, HashMap<String, double[]> tt) {
		String cur_pos_str = cur_pos.convert_string();
		if (cur_pos.is_terminal() != 0 || !tt.containsKey(cur_pos_str) || tt.get(cur_pos_str)[1] == 0) {
			return true;
		}
		List<int[]> moves = cur_pos.get_legal_moves();
		for (int[] move : moves) { 
			if (!tt.containsKey(cur_pos.make_move(move).convert_string())) {
				return true;
			}
		}
		return false;
	}
	
	public static int[] get_move_minimax_mcts(Pentago.Position pos) {
		long start_time = System.currentTimeMillis();
		List<int[]> starting_moves = get_best_moves_with_minimax(pos);
		return run_mcts(pos, start_time, starting_moves);	
	}

	public static int[] run_mcts(Pentago.Position pos, long start_time, List<int[]> starting_moves) {
		if (starting_moves.size() == 0) {
			return pos.get_legal_moves().get((int) (Math.random() * pos.get_legal_moves().size()));
		}
		if (starting_moves.size() == 1) { 
			return starting_moves.get(0);
		}
		
		int player = pos.get_next_player();
		double bf = LOOP_BRANCHING_FACTOR;
		
		HashMap<String, double[]> tt = new HashMap<String, double[]>();
		HashMap<String, List<int[]>> position_moves = new HashMap<String, List<int[]>>();

		tt.put(pos.convert_string(), new double[2]);
		position_moves.put(pos.convert_string(), starting_moves);
		
		// run for at most TIME_PER_MOVE_MILLI seconds
		while (System.currentTimeMillis() - start_time <= TIME_PER_MOVE_MILLI) {
			Pentago.Position cur_pos = pos;
			ArrayList<Pentago.Position> visited_pos = new ArrayList<Pentago.Position>();
			visited_pos.add(pos);
			
			while (!is_leaf_or_terminal(cur_pos, tt, position_moves)) {
				boolean max_pos = true;
				if (cur_pos.get_next_player() != player) {
					max_pos = false;
				}
				int[] best_move = get_best_move(cur_pos, position_moves.get(cur_pos.convert_string()), max_pos, tt);
				cur_pos = cur_pos.make_move(best_move);
				visited_pos.add(cur_pos);
				
				bf *= bf_constant;
			}
			
			if (cur_pos.is_terminal() != 0) {
				backtracking(tt, visited_pos, player, cur_pos.is_terminal());
			} else {
				if (!tt.containsKey(cur_pos.convert_string())) {
					tt.put(cur_pos.convert_string(), new double[2]);
				}
				
				if (!position_moves.containsKey(cur_pos.convert_string())) {
					position_moves.put(cur_pos.convert_string(), cur_pos.get_subset_of_moves((int) bf));
				}
				
				List<int[]> child_moves = position_moves.get(cur_pos.convert_string());
				
				// initialize children
				List<Pentago.Position> uninitialized_children = new ArrayList<Pentago.Position>();
				for (int[] child_move : child_moves) {
					Pentago.Position child_pos = cur_pos.make_move(child_move);
					if (!tt.containsKey(child_pos.convert_string())) {
						tt.put(child_pos.convert_string(), new double[2]);
						uninitialized_children.add(child_pos);
					}
				}
				
				Pentago.Position playout_pos;
				if (uninitialized_children.size() > 0) {
					playout_pos = uninitialized_children.get((int) (Math.random() * uninitialized_children.size()));
				} else {
					int[] random_move = child_moves.get((int) (Math.random() * child_moves.size()));
					playout_pos = cur_pos.make_move(random_move);
				}
				visited_pos.add(playout_pos);
				
				int winner = playout(playout_pos);
				backtracking(tt, visited_pos, player, winner);
			}
		}

		return get_best_starting_move(pos, position_moves.get(pos.convert_string()), tt);
	}
	
	public static int[] get_best_starting_move(Pentago.Position pos, List<int[]> moves, HashMap<String, double[]> tt) {
		double best_value = -1 * MAX_VALUE;
		int[] best_move = moves.get((int) (Math.random() * moves.size()));
		for (int[] move : moves) {
			double[] stats = tt.get(pos.make_move(move).convert_string());
			if (stats != null && stats[1] > 0) {
				double avg = stats[0] / stats[1];
				if (avg > best_value) {
					best_value = avg;
					best_move = move;
				}
			}
		}
		return best_move;
	}
	
	public static int playout(Pentago.Position playout_pos) {
		while (playout_pos.is_terminal() == 0) {
			List<int[]> moves = playout_pos.get_legal_moves();
			int[] random_move = moves.get((int) (Math.random() * moves.size()));
			playout_pos = playout_pos.make_move(random_move);
		}
		return playout_pos.is_terminal();
	}
	
	public static void backtracking(HashMap<String, double[]> tt, ArrayList<Pentago.Position> visited_pos, int player, int winner) {
		double bonus = 0;
		if (winner == 3) {
			bonus = .5;
		} else if (player == winner) {
			bonus = 1;
		}
		for (Pentago.Position pos : visited_pos) {
			double[] stats = tt.get(pos.convert_string());
			stats[0] += bonus;
			stats[1]++;
		}
	}
	
	public static boolean is_leaf_or_terminal(Pentago.Position cur_pos, HashMap<String, double[]> tt, HashMap<String, List<int[]>> position_moves) {
		String cur_pos_str = cur_pos.convert_string();
		if (cur_pos.is_terminal() != 0 || !tt.containsKey(cur_pos_str) || tt.get(cur_pos_str)[1] == 0 || !position_moves.containsKey(cur_pos_str)) {
			return true;
		}
		List<int[]> moves = position_moves.get(cur_pos_str);
		for (int[] move : moves) { 
			if (!tt.containsKey(cur_pos.make_move(move).convert_string())) {
				return true;
			}
		}
		return false;
	}
	
	public static int[] get_best_move(Pentago.Position cur_pos, List<int[]> moves, boolean max_pos, HashMap<String, double[]> tt) {
		double best_value = -1 * MAX_VALUE;
		int[] best_move = moves.get((int) (Math.random() * moves.size()));
		double[] parent_stats = tt.get(cur_pos.convert_string());
		
		for (int[] move : moves) {
			Pentago.Position child_pos = cur_pos.make_move(move);
			double ucb = get_ucb_value(parent_stats, tt.get(child_pos.convert_string()), max_pos);
			if (ucb > best_value) {
				best_value = ucb;
				best_move = move;
				if (ucb == MAX_VALUE) {
					break;
				}
			}
		}
		
		return best_move;
	}
	
	public static double get_ucb_value(double[] parent_stats, double[] child_stats, boolean max_pos) {
		if (child_stats[1] == 0) {
			return MAX_VALUE;
		}
		
		double exploit = child_stats[0] / child_stats[1];
		double explore = Math.sqrt(TUNABLE_CONSTANT * Math.log(parent_stats[1]) / child_stats[1]);
		
		if (max_pos) {
			return exploit + explore;
		}
		return (-1 * exploit) + explore;
	}
	
	// Runs depth 2 minimax to obtain the list of the best BRANCHING_FACTOR moves
	public static List<int[]> get_best_moves_with_minimax(Pentago.Position pos) {		
		List<int[]> best_moves = new ArrayList<int[]>();
		List<int[]> moves = pos.get_legal_moves();
		List<int[][]> moves_with_stats = new ArrayList<int[][]>();
		for (int[] move : moves) {
			Pentago.Position new_pos = pos.make_move(move);
			int winner = new_pos.is_terminal(); 
			if (winner == 0) {
				int non_wins = 0;
				
				List<int[]> new_pos_moves = new_pos.get_legal_moves();
				for (int[] new_move : new_pos_moves) {
					int next_winner = new_pos.make_move(new_move).is_terminal();
					if (new_pos.get_next_player() != next_winner) {
						non_wins++;
					}
				}
				
				int[][] move_with_stats = new int[2][4];
				move_with_stats[0] = move;
				move_with_stats[1][0] = non_wins;
				moves_with_stats.add(move_with_stats);
			} else {
				if (pos.get_next_player() == winner) {
					best_moves.clear();
					best_moves.add(move);
					return best_moves;
				} else if (winner == 3) {
					best_moves.add(move);
				}
			}
		}
		
		Collections.sort(moves_with_stats, new Comparator<int[][]>() {
		   public int compare(int[][] move1, int[][] move2) {
			   if (move1[1][0] > move2[1][0]) {
				   return -1;
			   }
			   if (move1[1][0] < move2[1][0]) {
				   return 1;
			   }
			   return 0;
		   }
		});
		
		int size = best_moves.size();
		int i = 0;
		while (i < moves_with_stats.size() && size < STARTING_BRANCHING_FACTOR) {
			best_moves.add(moves_with_stats.get(i)[0]);
			i++;
			size++;
		}
		
		return best_moves;
	}
}
