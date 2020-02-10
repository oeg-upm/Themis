/*
 * Copyright 2012-2013 Ontology Engineering Group, Universidad Politecnica de Madrid, Spain
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package oeg.albafernandez.tests.utils;


/**
 * Class for defining the constants
 */
public class TextConstants {

    public static final String header = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "  <head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <title>Glossary of terms</title>\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <meta name=\"languaje\" content=\"English\">\n" +
            "    \n" +
            "    <link rel=\"stylesheet\" href=\"vocab/themes/blue/style.css\" type=\"text/css\" media=\"print, projection, screen\" />\n" +
            "    <script src=\"vocab/js/jquery-1.11.0.js\"></script>\n" +
            "    <script type=\"text/javascript\" src=\"vocab/js/jquery.tablesorter.min.js\"></script>\n" +
            "    <script type=\"text/javascript\" src=\"vocab/js/jquery.stickytableheaders.js\"></script>\n" +
            "    <script type=\"text/javascript\" src=\"vocab/js/jquery-ui.js\"></script>\n" +
            "    <script type=\"text/javascript\" src=\"vocab/js/bootstrap.js\"></script>\n" +
            "    <link rel=\"stylesheet\" href=\"vocab/css/jquery-ui.css\"></link>\n" +
            "    <script type=\"text/javascript\" id=\"js\">\n" +
            "	    $(document).ready(function() \n" +
            "		    { \n" +
            "		    	$(\"#tablesorter-demo\").tablesorter(); \n" +
            "		    	$(\"#tablesorter-demo\").stickyTableHeaders(); \n" +
            "		    	$('[data-toggle=\"tooltip\"]').tooltip(); \n" +

            "		    } \n" +
            "	    ); \n" +
            "    </script>\n" +

            "\n" +
            "    <!-- Le styles -->\n" +
            "    <link href=\"vocab/css/bootstrap.css\" rel=\"stylesheet\">\n" +
            "    <style type=\"text/css\">\n" +
            "      body {\n" +
            "        padding-top: 60px;\n" +
            "        padding-bottom: 40px;\n" +
            "      }\n" +
            ".loader {\n" +
            "            text-align: center;\n" +
            "        }\n" +
            "        .loader span {\n" +
            "            display: inline-block;\n" +
            "            vertical-align: middle;\n" +
            "            width: 5px;\n" +
            "            height: 5px;\n" +
            "            margin: 5px auto;\n" +
            "            background: black;\n" +
            "            border-radius: 5px;\n" +
            "            -webkit-animation: loader 0.9s infinite alternate;\n" +
            "            -moz-animation: loader 0.9s infinite alternate;\n" +
            "        }\n" +
            "        .loader span:nth-of-type(2) {\n" +
            "            -webkit-animation-delay: 0.3s;\n" +
            "            -moz-animation-delay: 0.3s;\n" +
            "        }\n" +
            "        .loader span:nth-of-type(3) {\n" +
            "            -webkit-animation-delay: 0.6s;\n" +
            "            -moz-animation-delay: 0.6s;\n" +
            "        }\n" +
            "        @-webkit-keyframes loader {\n" +
            "            0% {\n" +
            "                width: 5px;\n" +
            "                height: 5px;\n" +
            "                opacity: 0.9;\n" +
            "                -webkit-transform: translateY(0);\n" +
            "            }\n" +
            "            100% {\n" +
            "                width: 5px;\n" +
            "                height: 5px;\n" +
            "                opacity: 0.1;\n" +
            "                -webkit-transform: translateY(-10px);\n" +
            "            }\n" +
            "        }"+
            "    </style>\n" +
            //"    <link href=\"vocab/css/bootstrap-responsive.css\" rel=\"stylesheet\">\n" +
            "    \n" +
            "    <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->\n" +
            "    <!--[if lt IE 9]>\n" +
            "      <script src=\"vocab/js/html5shiv.js\"></script>\n" +
            "    <![endif]-->\n" +
            "\n" +
            "    <!-- Fav and touch icons -->\n" +
            "  </head>\n" +
            "\n" +
            "  <body>\n" +
            "\n";


    public static String jumbotron =
            "    <div class=\"container\">\n" +
                    "\n" +
                    "      <!-- Jumbotron -->\n" +
                    "      <div class=\"jumbotron\">\n" +
                    "        <h2>Glossary of terms</h2>\n" +
                    "       </div>\n"+
                    "      <hr>\n";


    public static String button(String uri ){
        return  "     <div class=\"col-md-12 text-right\" >\n" +
                "            <button id=\"updategot\"  class=\"btn btn-primary mb-2\"  onclick=\"editoGoT('"+uri+"')\">Save changes</button>\n" +
                "\n" +
                "      </div>\n" +
                " <br>   ";
    }



    public static String headclasseswithkey(String key ){
        return  "      <div>\n" +
                //"        <h4>Classes</h4>  \n" +
                "      </div>\n" +
                "      <div id=\"got \" class=\"table-wrapper-scroll-y\">\n" +
                "      <table  class = \"got\"  name=\"tablegot\" id=\""+key+"\" class=\"table table-bordered table-responsive \" style=\"back\" >\n" +
                "      <thead>\n" +
                "       <tr class = \"got\" ><th class=\"col-md-3\">Term <span class=\"glyphicon glyphicon-pencil\"></span></th><th class=\"col-md-5\">URI</th><th class=\"col-md-2\"></th></tr></thead>\n" +
                "      <tbody>\n";

    }



    public static final String endTables="      </tbody>\n" +
            "\n" +
            "    </table>\n"+
            "      </div>\n";



}
