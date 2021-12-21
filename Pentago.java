import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pentago {
	public Pentago() {
	}
	
	public Position get_initial_pos() {
		int[][] board = new int[6][6];
		return new Position(board, 0);
	}
	
	public class Position {
		// 6x6 grid, 0 = no marble, 1 = white, 2 = black
		int[][] board;
		int num_marbles;
		
		public Position(int[][] board, int num_marbles) {
			this.board = board;
			this.num_marbles = num_marbles;
		}
		
		public String convert_string() {
			String out = "";
			for (int r = 0; r < 6; r++) {
				for (int c = 0; c < 6; c++) {
					out += board[r][c];
				}
			}
			return out;
		}
		
		@Override
	    public boolean equals(Object o) {
	 
	        // If the object is compared with itself then return true 
	        if (o == this) {
	            return true;
	        }
	 
	        /* Check if o is an instance of Complex or not
	          "null instanceof [type]" also returns false */
	        if (!(o instanceof Position)) {
	            return false;
	        }
	         
	        // typecast o to Complex so that we can compare data members
	        Position p = (Position) o;
	        
	        if (this.num_marbles != p.num_marbles) {
				return false;
			}
			for (int r = 0; r < 6; r++) {
				for (int c = 0; c < 6; c++) {
					if (this.board[r][c] != p.board[r][c]) {
						return false;
					}
				}
			}
	        return true;
	    }
		
		// white always goes first, num_marbles % 2 == 0 => white's turn
		// 1 = white, 2 = black
		public int get_next_player() {
			return 1 + (num_marbles % 2);
		}
		
		/*
		 * array of int[], where each int[] is a possible move
		 * int[0] = row
		 * int[1] = col
		 * int[2] = quadrant to rotate ((1, 1) = 0, (4, 1) = 1, (4, 4) = 2, (1, 4) = 3)
		 * int[3] = direction to rotate (0 = CCW, 1 = CW)
		 */
		public List<int[]> get_legal_moves() {
			List<int[]> moves = new ArrayList<int[]>();
			
			for (int r = 0; r < 6; r++) {
				for (int c = 0; c < 6; c++) {
					if (board[r][c] == 0) {
						for (int q = 0; q < 4; q++) {
							for (int d = 0; d < 2; d++) {
								int[] move = {r, c, q, d};
								moves.add(move);
							}
						}
					}
				}
			}
			
			return moves;
		}
		
		public List<int[]> get_subset_of_moves(int num_moves) {
			List<int[]> moves = get_legal_moves();
			Collections.shuffle(moves);
			return moves.subList(0, Math.min(num_moves, moves.size()));
		}
		
		// returns 0 if non-terminal, 1 if white wins, 2 if black wins, 3 if draw
		public int is_terminal() {
			// horizontal lines
			for (int r = 0; r < 6; r++) {
				int color = board[r][1];
				
				if (color == 0) {
					continue;
				}
				
				boolean passed = true;
				
				for (int c = 2; c <= 4; c++) {
					if (board[r][c] != color) {
						passed = false;
						break;
					}
				}
				
				if (passed) {
					if (board[r][0] == color || board[r][5] == color) {
						return color;
					}
				}
			}
			
			// vertical lines
			for (int c = 0; c < 6; c++) {
				int color = board[1][c];
				
				if (color == 0) {
					continue;
				}
				
				boolean passed = true;
				
				for (int r = 2; r <= 4; r++) {
					if (board[r][c] != color) {
						passed = false;
						break;
					}
				}
				
				if (passed) {
					if (board[0][c] == color || board[5][c] == color) {
						return color;
					}
				}
			}
			
			int[][] start_ll_diag = {{4, 0}, {5, 0}, {4, 1}, {5, 1}};
			for (int i = 0; i < start_ll_diag.length; i++) {
				int color = board[start_ll_diag[i][0]][start_ll_diag[i][1]];
				
				if (color == 0) {
					continue;
				}
				
				for (int j = 1; j <= 4; j++) {
					if (board[start_ll_diag[i][0] - j][start_ll_diag[i][1] + j] != color) {
						break;
					}
					if (j == 4) {
						return color;
					}
				}
			}
			
			int[][] start_ul_diag = {{0, 1}, {0, 0}, {1, 0}, {1, 1}};
			for (int i = 0; i < start_ul_diag.length; i++) {
				int color = board[start_ul_diag[i][0]][start_ul_diag[i][1]];
				
				if (color == 0) {
					continue;
				}
				
				for (int j = 1; j <= 4; j++) {
					if (board[start_ul_diag[i][0] + j][start_ul_diag[i][1] + j] != color) {
						break;
					}
					if (j == 4) {
						return color;
					}
				}
			}
			
			if (num_marbles == 36) {
				return 3;
			}
			
			return 0;
		}
		
		public void display_position() {
			for (int r = 0; r < 6; r++) {
				String out = "";
				for (int c = 0; c < 6; c++) {
					if (board[r][c] == 0) {
						out += ". ";
					} else if (board[r][c] == 1) {
						out += "W ";
					} else {
						out += "B ";
					}
					
					if (c == 2) {
						out += "| ";
					}
				}
				System.out.println(out);
				
				if (r == 2) {
					System.out.println("--------------");
				}
			}
			System.out.println();
		}
		
		public boolean is_valid_move(int[] move) {
			if (board[move[0]][move[1]] == 0) {
				return true;
			}
			return false;
		}
		
		public Position make_move(int[] move) {
			if (!is_valid_move(move)) {
				return null;
			}
			
			// int[][] new_board = board.clone();
			int[][] new_board = new int[6][6];
			for (int i = 0; i < 6; i++) {
				for (int j = 0; j < 6; j++) {
					new_board[i][j] = board[i][j];
				}
			}
			
			int turn = get_next_player();
			new_board[move[0]][move[1]] = turn;
			
			// rotate
			int quadrant = move[2];
			int[] center_quadrant = new int[2];
			if (quadrant == 0) {
				center_quadrant[0] = 1;
				center_quadrant[1] = 1;
			} else if (quadrant == 1) {
				center_quadrant[0] = 4;
				center_quadrant[1] = 1;
			} else if (quadrant == 2) {
				center_quadrant[0] = 4;
				center_quadrant[1] = 4;
			} else {
				center_quadrant[0] = 1;
				center_quadrant[1] = 4;
			}
			
			if (move[3] == 0) { // CCW
				// corners
				int save = new_board[center_quadrant[0] - 1][center_quadrant[1] - 1];
				new_board[center_quadrant[0] - 1][center_quadrant[1] - 1] = new_board[center_quadrant[0] - 1][center_quadrant[1] + 1];
				new_board[center_quadrant[0] - 1][center_quadrant[1] + 1] = new_board[center_quadrant[0] + 1][center_quadrant[1] + 1];
				new_board[center_quadrant[0] + 1][center_quadrant[1] + 1] = new_board[center_quadrant[0] + 1][center_quadrant[1] - 1];
				new_board[center_quadrant[0] + 1][center_quadrant[1] - 1] = save;
				
				// non-corners
				save = new_board[center_quadrant[0] - 1][center_quadrant[1]];
				new_board[center_quadrant[0] - 1][center_quadrant[1]] = new_board[center_quadrant[0]][center_quadrant[1] + 1];
				new_board[center_quadrant[0]][center_quadrant[1] + 1] = new_board[center_quadrant[0] + 1][center_quadrant[1]];
				new_board[center_quadrant[0] + 1][center_quadrant[1]] = new_board[center_quadrant[0]][center_quadrant[1] - 1];
				new_board[center_quadrant[0]][center_quadrant[1] - 1] = save;
			} else { // CW
				// corners
				int save = new_board[center_quadrant[0] - 1][center_quadrant[1] - 1];
				new_board[center_quadrant[0] - 1][center_quadrant[1] - 1] = new_board[center_quadrant[0] + 1][center_quadrant[1] - 1];
				new_board[center_quadrant[0] + 1][center_quadrant[1] - 1] = new_board[center_quadrant[0] + 1][center_quadrant[1] + 1];
				new_board[center_quadrant[0] + 1][center_quadrant[1] + 1] = new_board[center_quadrant[0] - 1][center_quadrant[1] + 1];
				new_board[center_quadrant[0] - 1][center_quadrant[1] + 1] = save;
				
				// non-corners
				save = new_board[center_quadrant[0] - 1][center_quadrant[1]];
				new_board[center_quadrant[0] - 1][center_quadrant[1]] = new_board[center_quadrant[0]][center_quadrant[1] - 1];
				new_board[center_quadrant[0]][center_quadrant[1] - 1] = new_board[center_quadrant[0] + 1][center_quadrant[1]];
				new_board[center_quadrant[0] + 1][center_quadrant[1]] = new_board[center_quadrant[0]][center_quadrant[1] + 1];
				new_board[center_quadrant[0]][center_quadrant[1] + 1] = save;
			}
			
			return new Position(new_board, num_marbles + 1);
		}
	}

}
