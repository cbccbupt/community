package com.nowconder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    // 替换字符
    private static final String REPLACEMENT = "***";

    // 根节点
    private TrieNode rootnode = new TrieNode();

    // 前缀树初始化
    @PostConstruct
    public void init(){
        try(
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt"); // 从txt文件中读取敏感词
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ){
            String keyword;
            while((keyword = reader.readLine()) != null){
                // 添加到前缀树
                this.addKeyWord(keyword);
            }
        }
        catch(IOException e){
            logger.error("加载敏感词文件失败" + e.getMessage());
        }
    }

    // 将一个敏感词降入到前缀树中
    private void addKeyWord(String keyword){
        TrieNode tempNode = rootnode;
        for(int i=0;i<keyword.length();i++){
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c); // 判断是否有key为该字符的子节点

            if(subNode==null){
                // 没有该字符的子节点，创建子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c,subNode);
            }

            // 指针指向子节点，进入第二轮循环
            tempNode = subNode;

            // 若该节点为敏感词结尾，设置标记
            if(i == keyword.length()-1){
                tempNode.setKeyWordEnd(true);
            }
        }
    }

    /**
     * 判断某个词是否为敏感词
     * @param text 待检查词汇
     * @return 替换敏感词后的字符
     */
    public String filter(String text){
        if(StringUtils.isBlank(text)){
            return null;
        }

        // 指针1
        TrieNode tempNode = rootnode;
        // 指针2
        int begin = 0;
        // 指针3
        int position = 0;
        // 过滤结果
        StringBuilder sb = new StringBuilder();

        while (begin < text.length()){
            if(position < text.length()){
                Character c = text.charAt(position);

                // 若当前位置为特殊符号则跳过
                if(isSymbol(c)){
                    if(tempNode==rootnode){
                        begin++;
                        sb.append(c);
                    }
                    position++;
                    continue;
                }

                // 检查下级节点
                tempNode = tempNode.getSubNode(c);
                if(tempNode==null){
                    // 以begin为开头的字符不是敏感词汇,将begin处的字符加入结果中
                    sb.append(text.charAt(begin));
                    // 进入下一位置
                    begin++;
                    position=begin;
                    // 指针1回到根节点
                    tempNode = rootnode;
                }
                // 发现敏感词
                else if(tempNode.isKeyWordEnd==true){
                    sb.append(REPLACEMENT);
                    position++;
                    begin=position;
                }
                // 未发现敏感词，且字符不是敏感词结尾
                else{
                    position++;
                }
            }
            // 指针position越界仍未找到敏感词
            else{
                sb.append(text.charAt(begin));
                begin++;
                position=begin;
                tempNode=rootnode;
            }
        }
        return sb.toString();
    }

    // 判断是否为特殊字符
    private boolean isSymbol(Character c){
        // 0x2E80~0x9FFF 时东亚字符范围，应在特殊字符判断范围外
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    // 前缀树定义
    private class TrieNode{

        // 节点是否为敏感词结尾
        private boolean isKeyWordEnd = false;

        // 该节点的子节点（Key表示子节点的字符，Value表示子节点）
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode node){
            subNodes.put(c,node);
        }

        // 获取子节点
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }
    }
}
