public class AccountThread extends Thread {
    private String name;
    private byte[] nameMap;
    public AccountThread(String name, byte[] nameMap) {
        this.name = name;
        this.nameMap = nameMap;
    }

    @Override
    public void run() {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        TxtFileUtils.getAcctontsMmb3(name,nameMap);
    }
}

