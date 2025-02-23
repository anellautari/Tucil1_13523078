import java.io.*;
import java.util.*;

public class IQPuzzlerPro {
    public static final String RESET = "\033[38;5;0m";
    public static final String[] ANSI_COLORS = {
        "\033[38;5;196m", "\033[38;5;226m", "\033[38;5;46m", 
        "\033[38;5;208m", "\033[38;5;30m", "\033[38;5;99m", 
        "\033[38;5;51m", "\033[38;5;18m", "\033[38;5;81m", 
        "\033[38;5;90m","\033[38;5;21m", "\033[38;5;201m",
        "\033[38;5;206m","\033[38;5;130m","\033[38;5;218m",
        "\033[38;5;88m", "\033[38;5;180m", "\033[38;5;100m", 
        "\033[38;5;220m", "\033[38;5;22m", "\033[38;5;87m", 
        "\033[38;5;147m", "\033[38;5;15m", "\033[38;5;89m", 
        "\033[38;5;240m", "\033[38;5;252m"  
    };
    public static void main(String[] args){
        Map<Character, String> colorMap = new HashMap<>();
        for (int i = 0; i < 26; i++) {
            colorMap.put((char) ('A' + i), ANSI_COLORS[i % ANSI_COLORS.length]); 
        }
        
        Scanner scanner = new Scanner(System.in);
        System.out.print("Masukkan nama file txt: ");
        String file = scanner.nextLine();

        try (BufferedReader input = new BufferedReader(new FileReader(file))){

            String[] baris1 = input.readLine().split(" ");

            int N = Integer.parseInt(baris1[0]);
            int M = Integer.parseInt(baris1[1]);
            int P = Integer.parseInt(baris1[2]);

            char[][] board = new char[N][M];
            for (int i = 0; i < N; i++) {
                Arrays.fill(board[i], '.'); // Isi semua sel dengan spasi (kosong)
            }

            String S = input.readLine().trim();

            List<char[][]> allpuzzles = new ArrayList<>();
            String baris;
            List<String> currentPuzzle = new ArrayList<>();

            while ((baris = input.readLine()) != null) {
                if (baris.trim().isEmpty()){
                    if (!currentPuzzle.isEmpty()){
                        allpuzzles.add(convertStringToChar(currentPuzzle));
                        currentPuzzle.clear();
                    }
                } else {
                    currentPuzzle.add(baris);
                }
            }
            if (!currentPuzzle.isEmpty()) allpuzzles.add(convertStringToChar(currentPuzzle));

            input.close();

            printPuzzles(allpuzzles, colorMap);

            if (allpuzzles.isEmpty()) {
                System.out.println("Error: Tidak ada puzzle yang terbaca dari file.");
                return;
            }            

            if (!solve(0, allpuzzles, board, N, M)){
                System.out.println("Tidak ada solusi.");
            }

        } catch (IOException e){
            System.out.println("Error membaca file: " + e.getMessage());
        }
    }

    private static char[][] convertStringToChar(List<String> puzzle){
        char[][] array = new char[puzzle.size()][];
        for (int i=0;i<puzzle.size();i++){
            array[i] = puzzle.get(i).toCharArray();
        }
        return array;
    }

    // debug
    private static void printPuzzles(List<char[][]> allpuzzles, Map<Character, String> colorMap) {
        System.out.println("\n=== Puzzles ===");
        for (int i = 0; i < allpuzzles.size(); i++) {
            //System.out.println("Puzzle " + (i + 1) + ":");
            for (char[] row : allpuzzles.get(i)) {
                // Cek isi row sebelum ditampilkan
                //System.out.println(Arrays.toString(row)); // Debug: Print array as is
                for (char c : row) {
                    String color = colorMap.get(c); // Ambil warna dari colorMap
                    System.out.print(color + c + "\u001B[0m"); // Warna + Huruf + Reset
                }                
                System.out.println();
            }
            //System.out.println();
        }
        System.out.println("=== Debug: Semua Puzzle ===");
        for (char[][] p : allpuzzles) {
            for (char[] row : p) {
                System.out.println(Arrays.toString(row));
            }
            System.out.println();
        }

    }    
    private static boolean solve(int index, List<char[][]> allpuzzles, char[][] board, int N, int M) {
        System.out.println("Mencoba puzzle ke-" + index);
        printBoard(board);

        if (index == allpuzzles.size()) {
            printBoard(board);
            return true; // Semua puzzle sudah ditempatkan
        }
    
        char[][] puzzle = allpuzzles.get(index);
    
        // Coba semua posisi di papan
        for (int i=0;i<N;i++) {
            for (int j=0;j<M;j++) {
                for (int k=0;k<4;k++) { // coba semua rotasi
                    char[][] rotated = rotate(puzzle,k);
                    char[][] flippedH = flipHorizontal(rotated);
                    char[][] flippedV = flipVertical(rotated);
                    if (canPlace(rotated, board, i, j, N, M)) {
                        placePuzzle(rotated, board, i, j);
                        if (tryPlacement(rotated, board, i, j, N, M, index, allpuzzles)) return true;
                        removePuzzle(rotated, board, i, j); // Backtrack
                    }
                    
                    if (canPlace(flippedH, board, i, j, N, M)) {
                        placePuzzle(flippedH, board, i, j);
                        if (tryPlacement(flippedH, board, i, j, N, M, index, allpuzzles)) return true;
                        removePuzzle(flippedH, board, i, j); // Backtrack
                    }
                    
                    if (canPlace(flippedV, board, i, j, N, M)) {
                        placePuzzle(flippedV, board, i, j);
                        if (tryPlacement(flippedV, board, i, j, N, M, index, allpuzzles)) return true;
                        removePuzzle(flippedV, board, i, j); // Backtrack
                    }                    
                }
            }
        }
        return false; // Tidak ada solusi dari posisi ini
    }    

    private static boolean tryPlacement(char[][] puzzle, char[][] board, int i, int j, int N, int M, int index, List<char[][]> allpuzzles){
        if (solve(index + 1, allpuzzles, board, N, M)) {
            return true;
        }
        removePuzzle(puzzle, board, i, j); // Backtrack kalau gagal
        return false;
    }

    private static boolean canPlace(char[][] puzzle, char[][] board, int x, int y, int N, int M){
        for (int i=0;i<puzzle.length;i++){
            for (int j=0;j<puzzle[i].length;j++){
                if (puzzle[i][j] != ' ' && (x+i >= N || y+j >= M || board[x+i][y+j] != '.')){
                    System.out.println("Tidak bisa menempatkan di (" + x + "," + y + ")");
                    return false; 
                }
            }
        }
        System.out.println("Bisa menempatkan di (" + x + "," + y + ")");
        return true;
    }

    private static void placePuzzle (char[][] puzzle, char[][] board, int x, int y){
        System.out.println("Mencoba tempatkan puzzle di (" + x + "," + y + ")");

        for (int i=0;i<puzzle.length;i++){
            for (int j=0;j<puzzle[i].length;j++){
                if (puzzle[i][j] != ' '){
                    board[x+i][y+j] = puzzle[i][j];
                }
            }
        }
    }

    private static void removePuzzle (char[][] puzzle, char[][] board, int row, int col){
        System.out.println("Menghapus puzzle dari (" + row + "," + col + ")");

        for (int i=0;i<puzzle.length;i++){
            for (int j=0;j<puzzle[i].length;j++){
                if (puzzle[i][j] != ' '){
                    board[row+i][col+j] = '.';
                }
            }
        }
    }

    private static char[][] rotate(char[][] puzzle, int putar){
        // if (puzzle.length == 0 || puzzle[0].length == 0) {
        //     return puzzle; // Hindari error dengan langsung mengembalikan puzzle kosong
        // }
        //System.out.println("Rotating puzzle: " + puzzle.length + "x" + (puzzle.length > 0 ? puzzle[0].length : 0));
        
        char[][] rotated = puzzle;
        for (int i=0;i<putar;i++){

            int row = rotated.length;
            //if (row == 0) return puzzle;

            int col = rotated[0].length;
            //if (col == 0) return puzzle;

            char[][] newRotated = new char[col][row];

            for (int j=0;j<row;j++){
                for (int k=0;k<col;k++){
                    newRotated[k][row-j-1] = rotated[j][k];
                }
            }
            rotated = newRotated;
        }
        return rotated;
    }

    private static char[][] flipHorizontal(char[][] puzzle){
        int row = puzzle.length;
        char[][] flipped = new char[row][];
        for (int i=0;i<row;i++){
            flipped[i] = puzzle[row-i-1];
        }
        return flipped;
    }

    private static char[][] flipVertical(char[][] puzzle){
        int row = puzzle.length, col = puzzle[0].length;
        char[][] flipped = new char[row][col];
        for (int i=0;i<row;i++){
            for (int j=0;j<col;j++){
                flipped[i][col-j-1] = puzzle[i][j];
            }
        }
        return flipped;
    }

    private static void printBoard (char[][] board){
        System.out.println("\n=== Solusi Akhir ===");
        for (char[] row : board){
            System.out.println(Arrays.toString(row));
        }
    }
}