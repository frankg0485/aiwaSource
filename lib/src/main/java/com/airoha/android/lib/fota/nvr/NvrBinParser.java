package com.airoha.android.lib.fota.nvr;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NvrBinParser {

    private FileReader mFileReader;

    private List<NvrDescriptor> mListNvrDescriptor;

    private static String STR_START = "&";
    private static String STR_SPLIT = "=";

    public NvrBinParser(String fileName){
        try {
            mFileReader = new FileReader(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        mListNvrDescriptor = new ArrayList<>();
    }

    public void startParse() {
        // read line

        BufferedReader br = new BufferedReader(mFileReader);

        String strLine;

        try{
            while ((strLine = br.readLine()) != null){
                if(strLine.startsWith(STR_START)){
                    String[] strs = strLine.split(STR_SPLIT);

                    String nvKey = strs[0].trim().substring(3);
                    String nvValue = strs[1].trim();

                    NvrDescriptor nvrDescriptor = new NvrDescriptor(nvKey, nvValue);

                    mListNvrDescriptor.add(nvrDescriptor);
                }
            }

            br.close();
            mFileReader.close();

        } catch (IOException ioe){
            ioe.printStackTrace();
        }


    }

    public List<NvrDescriptor> getListNvrDescriptor() {
        return mListNvrDescriptor;
    }
}
