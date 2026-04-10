package com.octopuz.platform.service.interf;

public interface PythonScriptService {
    /*
    * 执行Python脚本
    * @param scriptPath(相对于scripts目录)
    */
    String executeScript(String scriptPath);
    /*
    * 执行Python脚本并传入参数
    * @param scriptPath(相对于scripts目录)
    * @param args，参数列表
    */
    String executeScriptWithArgs(String scriptPath, String... args);
}
