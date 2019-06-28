package com.dosimple.robot.util.words;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.ansj.app.keyword.KeyWordComputer;
import org.ansj.app.keyword.Keyword;
import org.ansj.recognition.impl.StopRecognition;
import org.apache.commons.lang3.StringUtils;
import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.Word;
import org.springframework.cglib.core.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * @author baolw
 */
public class WordTokenizerUtils {

    public static List<String> segWithStopWordsByWord(String words) {
        if (StringUtils.isBlank(words)) {
            return Lists.newArrayList();
        }
        List<Word> list = WordSegmenter.segWithStopWords(words);
        return CollectionUtils.transform(list, o -> {
            Word word = (Word) o;
            return word.getText();
        });
    }

    public static List<String> segWordsByAnsj(String words) {
        if (StringUtils.isBlank(words)) {
            return Lists.newArrayList();
        }
        return CollectionUtils.transform(new KeyWordComputer().computeArticleTfidf(words), o -> {
            Keyword word = (Keyword) o;
            return word.getName();
        });
    }

    /*
    计算余弦向量
    Cosθ  = (a1b1+a2b2 +a3b3 + .. anbn)/(sqrt(a1^2+a2^2+a3^2 + ... + an^2)*sqrt(b1^2+b2^2+b3^2 + .. + bn^2))
     */
    public static double cosVector(String[] wordsOfSen1, String[] wordsOfSen2) {
        //单词的出现频数，例：wordWeight[word][0]单词"word"在第一句中出现的频数
        Map<String, int[]> wordWeight = Maps.newHashMap();
        //两句话的单词频数统计
        for (String aWordsOfSen1 : wordsOfSen1) {
            if (!wordWeight.containsKey(aWordsOfSen1)) {
                wordWeight.put(aWordsOfSen1, new int[]{1, 0});
            } else {
                wordWeight.get(aWordsOfSen1)[0] += 1;
            }
        }
        for (String aWordsOfSen2 : wordsOfSen2) {
            if (!wordWeight.containsKey(aWordsOfSen2)) {
                wordWeight.put(aWordsOfSen2, new int[]{0, 1});
            } else {
                wordWeight.get(aWordsOfSen2)[1] += 1;
            }
        }
        //上面已经将各个单词的频数按照向量(即句子向量)的形式表示出来了
        //wordWeight.size就是向量的维数
        //wordWeight[word][0]就是单词"word"在第一句中出现的频数
        //下面利用该向量计算余弦
        double neiji = 0.0;//两个句子向量的内积
        double modeOfSen1 = 0.0;//句子1的向量模de平方
        double modeOfSen2 = 0.0;//句子2的向量模de平方
        for (String key : wordWeight.keySet()) {
            neiji += wordWeight.get(key)[0] * wordWeight.get(key)[1];
            modeOfSen1 += Math.pow(wordWeight.get(key)[0], 2);
            modeOfSen2 += Math.pow(wordWeight.get(key)[1], 2);
        }
        return neiji / (Math.sqrt(modeOfSen1) * Math.sqrt(modeOfSen2));
    }


}
