class WordScore {
    String word;
    int score;
    int start;
    int end;

    // Stores a candidate word together with its score and board start/end positions.
    WordScore(String word, int score, int start, int end) {
        this.word = word;
        this.score = score;
        this.start = start;
        this.end = end;
    }
}