public class UnionFind {
    int[] parent;
    int size;

    public UnionFind(int size) {
        this.size = size;
        this.parent = new int[size];
        for (int i = 0; i < size; i++) {
            parent[i] = i;
        }
    }

    public int find(int x) {
        if(x==parent[x]){
            return x;
        }else{
            x=parent[x];
            while (x != parent[x]) {
                x = parent[x];
            }
        }
        return x;
    }

    public void unionElements(int firstElement, int secondElement) {
        int firstRoot = find(firstElement);
        int secondRoot = find(secondElement);
        if(firstRoot<secondRoot){
            parent[secondRoot] = firstRoot;
            parent[firstElement] = firstRoot;
            parent[secondElement] = firstRoot;
        }else{
            parent[firstRoot] = secondRoot;
            parent[firstElement] = secondRoot;
           parent[secondElement] = secondRoot;
        }

    }
}

