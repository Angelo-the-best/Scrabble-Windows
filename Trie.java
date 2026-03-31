class TrieNode {
    TrieNode[] children = new TrieNode[26];
    boolean isEndOfWord;

    // Initialises a trie node with no children and marks it as not a word end.
    public TrieNode() {
        isEndOfWord = false;
        for (int i = 0; i < 26; i++) {
            children[i] = null;
        }
    }
}

public class Trie {
    private TrieNode root;

    // Initialises the trie with an empty root node.
    public Trie() {
        root = new TrieNode();
    }

    // Inserts a word into the trie, creating nodes for each character as needed.
    public void insert(String word) {
        TrieNode node = root;
        for (char c : word.toUpperCase().toCharArray()) {
            if (c < 'A' || c > 'Z') continue;
            int index = c - 'A';
            if (node.children[index] == null) {
                node.children[index] = new TrieNode();
            }
            node = node.children[index];
        }
        node.isEndOfWord = true;
    }

    // Returns true if the given word exists in the trie.
    public boolean search(String word) {
        TrieNode node = root;
        for (char c : word.toUpperCase().toCharArray()) {
            if (c < 'A' || c > 'Z') return false;
            int index = c - 'A';
            if (node.children[index] == null) {
                return false;
            }
            node = node.children[index];
        }
        return node.isEndOfWord;
    }
}