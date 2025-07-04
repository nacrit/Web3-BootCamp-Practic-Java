package com.nacrt.base;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Demo3MiniBlockChain {
    /**
     * 用自己熟悉的语言模拟实现最小的区块链， 包含两个功能：
     * POW 证明出块，难度为 4 个 0 开头
     * 每个区块包含 previous_hash 让区块串联起来。
     * 如下是一个参考区块结构：
     * block = {
     * 'index': 1,
     * 'timestamp': 1506057125,
     * 'transactions': [
     * { 'sender': "xxx",
     * 'recipient': "xxx",
     * 'amount': 5, } ],
     * 'proof': 324984774000,
     * 'previous_hash': "xxxx"
     * }
     * 请提交完成的 github 代码仓库链接， 在 Readme 中包含运行说明及运行日志或截图。
     */
    public static void main(String[] args) {
        BlockChain blockchain = new BlockChain(); // 设置难度为4个0
        System.out.println("==== 初始区块链 ====");
        blockchain.printChain();

        System.out.println("\n==== 第一次挖矿 ====");
        Block block1 = blockchain.mineBlock("nacrt");
        blockchain.printChain();

        System.out.println("\n==== 添加交易后挖矿 ====");
        blockchain.createTransaction("nacrt", "alice", "10");
        blockchain.createTransaction("alice", "bob", "10");
        Block block2 = blockchain.mineBlock("miner2");
        blockchain.printChain();

        System.out.println("\n==== 继续挖矿5次 ====");
        for (int i = 0; i < 5; i++) {
            blockchain.mineBlock("nacrt" + i);
            blockchain.printChain();
        }

        System.out.println("\n==== 区块链验证 ====");
        boolean valid = blockchain.validateChain();
        System.out.println("区块链" + (valid ? "有效 ✅" : "无效 ❌"));

    }

    static class BlockChain {
        // 区块链
        private final List<Block> blocks = new ArrayList<>();
        // pending中的交易
        private final List<Transaction> pendingTransactions = new ArrayList<>();
        int difficulty = 4; // 几个0开头的hash

        public BlockChain() {
            // 生成创世区块
            createGenesisBlock();
        }

        private void createGenesisBlock() {
            List<Transaction> genesisTxs = Collections.singletonList(new Transaction("0", "nacrt", BigDecimal.ZERO));
            Block genesisBlock = new Block(0, genesisTxs, 0L, "0000000000000000000000000000000000000000000000000000000000000000");
            blocks.add(genesisBlock);
        }

        // 将新交易到待处理交易列表
        public void createTransaction(String sender, String recipient, String amount) {
            pendingTransactions.add(new Transaction(sender, recipient, new BigDecimal(amount)));
        }

        public Block mineBlock(String minter) {
            // 获取最后一个区块信息
            Block lastBlock = getLastBlock();
            Transaction mintTx = new Transaction("0", minter, new BigDecimal("50"));
            return proofOfWork(lastBlock.hash, mintTx);
        }

        public Block getLastBlock() {
            return blocks.get(blocks.size() - 1);
        }

        private Block proofOfWork(String previousHash, Transaction mintTx) {
            List<Transaction> newTxs = new ArrayList<>();
            newTxs.add(mintTx);
            newTxs.addAll(pendingTransactions);
            // 新增一条挖矿奖励交易
            Block newBlock = new Block(blocks.size(), newTxs, previousHash);
            while (true) {
                String newBlockHash = newBlock.calculateHash();
                if (startWithNZero(difficulty, newBlockHash)) {
                    // 新的满足条件的hash
                    newBlock.hash = newBlockHash;
                    // 清空缓存交易
                    pendingTransactions.clear();
                    // 出块
                    blocks.add(newBlock);
                    return newBlock;
                }
                newBlock.proof++;
            }
        }


        public boolean validateChain() {
            for (int i = 1; i < blocks.size(); i++) {
                Block current = blocks.get(i);
                Block previous = blocks.get(i - 1);

                // 验证区块哈希
                if (!current.validateHash()) {
                    System.err.printf("区块 %d 哈希验证失败\n", current.index);
                    return false;
                }

                // 验证与前区块链接
                if (!current.previousHash.equals(previous.hash)) {
                    System.err.printf("区块 %d 与前区块链接错误\n", current.index);
                    return false;
                }

                // 验证工作量证明
                if (!current.hash.startsWith("0".repeat(difficulty))) {
                    System.err.printf("区块 %d 工作量证明不足\n", current.index);
                    return false;
                }
            }
            return true;
        }

        // 打印区块链
        public void printChain() {
            System.out.println("区块高度 | 区块哈希 | 前区块哈希 | 交易数量 | 工作量证明 | 时间");
            System.out.println("------------------------------------------------------------");
            for (Block block : blocks) {
                System.out.printf("%-8d %-10s %-10s %-6d %-10d %s\n",
                        block.index,
                        block.hash.substring(0, 8) + "...",
                        block.previousHash.substring(0, 8) + "...",
                        block.transactions.size(),
                        block.proof,
                        formatTime(block.timestamp));
            }
            System.out.println();
        }

        private String formatTime(Long timestamp) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            return sdf.format(new Date(timestamp));
        }

        @Override
        public String toString() {
            return "BlockChain{" +
                    "blocks=" + blocks +
                    ", pendingTransactions=" + pendingTransactions +
                    ", difficulty=" + difficulty +
                    '}';
        }
    }

    static class Block {
        private final Integer index;
        private final Long timestamp;
        private final List<Transaction> transactions;
        private final String txRootHash;
        private Long proof;
        private final String previousHash;
        private String hash;

        public Block(Integer index, List<Transaction> transactions, Long proof, String previousHash) {
            this.index = index;
            this.transactions = transactions;
            this.txRootHash = sha256(transactions.toString().getBytes());
            this.proof = proof;
            this.previousHash = previousHash;
            this.timestamp = System.currentTimeMillis();
            this.hash = calculateHash();
        }

        public Block(Integer index, List<Transaction> transactions, String previousHash) {
            this.index = index;
            this.transactions = transactions;
            this.txRootHash = sha256(transactions.toString().getBytes());
            this.previousHash = previousHash;
            this.timestamp = System.currentTimeMillis();
            this.proof = 0L;
        }

        public String calculateHash() {
            try {
                String input = index + timestamp + txRootHash + proof + previousHash;
                return sha256(input.getBytes());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String toString() {
            return "BlockChain{" +
                    "index=" + index +
                    ", timestamp=" + timestamp +
                    ", transactions=" + transactions +
                    ", proof=" + proof +
                    ", previousHash='" + previousHash + '\'' +
                    ", hash='" + hash + '\'' +
                    '}';
        }

        public boolean validateHash() {
            return hash.equals(calculateHash());
        }
    }

    static class Transaction {
        private final String sender;
        private final String recipient;
        private final BigDecimal amount;

        public Transaction(String sender, String recipient, BigDecimal amount) {
            this.sender = sender;
            this.recipient = recipient;
            this.amount = amount;
        }

        @Override
        public String toString() {
            return "{sender: '" + sender + "', recipient: '" + recipient + "', amount: " + amount + "}";
        }
    }

    private static String sha256(byte[] bytes) {
        try {
            MessageDigest instance = MessageDigest.getInstance("SHA-256");
            byte[] digest = instance.digest(bytes);
            return bytesToHexString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            hexString.append(hex.length() == 1 ? "0" + hex : hex);
        }
        return hexString.toString();
    }

    private static boolean startWithNZero(int n, String str) {
        String nZero = "0".repeat(n);
        return str.startsWith(nZero);
    }
}
