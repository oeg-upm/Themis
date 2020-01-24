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
 * @author mpoveda, dgarijo, albafernandez
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

    public static final String endbody = "  <footer class=\"footer\">\n" +
            "      <div class=\"row\">\n" +
            "        <div class=\"col-md-10\">\n" +
            "    \t\t    Developed by \t        <a href = \"http://oeg-upm.net\" target=\"_blank\">Ontology Engineering Group</a>\n" +
            "\t           <br>\n" +
            "          \tBuilt with <a target=\"_blank\" href=\"http://getbootstrap.com/\">Bootstrap</a>\n" +
            "          \tIcons from <a target=\"_blank\" href=\"http://glyphicons.com/\">Glyphicons</a>\n" +
            " \t          <br>\n" +
            "\t           Latest revision October, 2018\n" +
            "            <br><p>&copy; got.Ontology Engineering Group</p>\n" +
            "        </div>\n" +
            "        <div class=\"col-md-2\">\n" +
            "    \t\t  <a href=\"http://www.oeg-upm.net/\" target=\"_blank\"><img src=\"vocab/logo.gif\" alt=\"OEG logo\" class=\"img-rounded\" class=\"img-responsive\" /></a>\n" +
            "        </div>\n" +
            "      </div>\n" +
            "      </footer>\n" +
            "    </div> <!-- /container -->\n" +
            "</body>\n";

    public static final String endhtml =
            "</html>";

    public static final String definedTerms ="      <div>\n" +
            "        <h3>Defined terms</h3>  \n" +
            "      </div>\n";

    public static final String headclasses = "      <div>\n" +
            //"        <h4>Classes</h4>  \n" +
            "      </div>\n" +
            "      <div id=\"got \" class=\"table-wrapper-scroll-y\">\n" +
            "      <table    class = \"got\"  name=\"tablegot\" id=\"core\" class=\"table  table-responsive table-bordered \">\n" +
            "      <thead>\n" +
            "       <tr class = \"got table-secondary\" ><th class=\"col-md-4\">Term </th><th class=\"col-md-5\">URI</th></tr></thead>\n" +
            "      <tbody>\n";


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





    public static final String headclassesimported = "      <div>\n" +
            "        <h4>Imported Classes</h4>  \n" +
            "      </div>\n" +
            "      <div>\n" +
            "      <table  class=\"table table-hover table-responsive \">\n" +
            "      <thead>\n" +
            "       <tr><th class=\"col-md-2\">Term </th><th class=\"col-md-2\">URI</th></tr></thead>\n" +
            "      <tbody>\n";

    public static final String endTables="      </tbody>\n" +
            "\n" +
            "    </table>\n"+
            "      </div>\n";

    public static final String headobjprop= "    <div>\n" +
            "      <h4>Object Properties</h4>  \n" +
            "    </div>\n" +
            "    <div>\n" +
            "    <table class=\"table table-hover table-responsive\">\n" +
            "    <thead>\n" +
            "     <tr><th class=\" col-md-2\">Term </th><th class=\"col-md-2\">URI</th></tr></thead>\n" +
            "    <tbody>";
    public static final String headobjpropimported= "    <div>\n" +
            "      <h4>Imported Object Properties</h4>  \n" +
            "    </div>\n" +
            "    <div>\n" +
            "    <table class=\"table table-hover table-responsive\">\n" +
            "    <thead>\n" +
            "     <tr><th class=\"col-md-2\">Term </th><th class=\"col-md-2\">URI</th></tr></thead>\n" +
            "    <tbody>";

    public static final String headdataprop= "    <div>\n" +
            "      <h4>Datatype Properties</h4>  \n" +
            "    </div>\n" +
            "    <div>\n" +
            "    <table  class=\"table table-hover table-responsive\">\n" +
            "    <thead>\n" +
            "     <tr><th class=\"col-md-2\">Term </th><th class=\"col-md-2\">URI</th></tr></thead>\n" +
            "    <tbody> ";
    public static final String headdatapropimported= "    <div>\n" +
            "      <h4>Imported Datatype Properties</h4>  \n" +
            "    </div>\n" +
            "    <div>\n" +
            "    <table  class=\"table table-hover table-responsive\">\n" +
            "    <thead>\n" +
            "     <tr><th class=\"col-md-2\">Term </th><th class=\"col-md-2\">URI</th></tr></thead>\n" +
            "    <tbody> ";

    public static final String headindividuals= "    <div>\n" +
            "      <h4>Individuals</h4>  \n" +
            "    </div>\n" +
            "    <div>\n" +
            "    <table  class=\"table table-hover table-responsive\">\n" +
            "    <thead>\n" +
            "     <tr><th class=\"col-md-2\">Term </th><th class=\"col-md-2\">URI</th></tr></thead>\n" +
            "    <tbody> ";

    public static final String headimportedindividuals= "    <div>\n" +
            "      <h4>ImportedIndividuals</h4>  \n" +
            "    </div>\n" +
            "    <div>\n" +
            "    <table  class=\"table table-hover table-responsive\">\n" +
            "    <thead>\n" +
            "     <tr><th class=\"col-md-2\">Term </th><th class=\"col-md-2\">URI</th></tr></thead>\n" +
            "    <tbody> ";

    public static final String importedTerms = "    <div>\n" +
            "      <h3>Imported terms</h3>  \n" +
            "    </div>\n";

    public static  final String buttonGot = "<button id=\"submitgot\" class=\"label label-default\" " +
            "name=\"storegot\" value=\" Save changes \"  style=\"font-size: 75%; font-weight: bold; line-height: 1; \"></button>";

    public static final String scriptGot = "<script>\n" +
          "function editoGoT(uri) {\n" +
            "        $('#updategot').html(\"<div class=\\\"loader\\\">\\n\" +\n" +
            "            \"                        <span></span>\\n\" +\n" +
            "            \"                        <span></span>\\n\" +\n" +
            "            \"                        <span></span>\\n\" +\n" +
            "            \"                    </div>\");\n" +
            "        $('#updategot').attr(\"disabled\", true);"+
            "        var table = document.getElementById(\"got\");\n" +
            "var obj = {};\n" +
            "obj.uri = uri;\n" +


            "var myRows = [];\n" +
            "var $headers = $(\"th\");\n" +
            "var $rows = $(\"tbody tr\").each(function(index) {\n" +
            "  $cells = $(this).find(\"td\");\n" +
            "  myRows[index] = {};\n" +
            "  $cells.each(function(cellIndex) {\n" +
            "    myRows[index][$($headers[cellIndex]).html()] = $(this).html();\n" +
            "  });    \n" +
            "});\n" +
            "var myObj = {};\n" +
            "myObj.myrows = myRows;\n" +
            "obj.table = JSON.stringify(myRows);\n" +
            "        $.ajax({\n" +
            "            type: 'POST',\n" +
            "            dataType: \"json\",\n" +
            "            contentType: \"application/json\"," +
            "            data: JSON.stringify(obj),\n" +
            "            url: 'http://localhost:8080/rest/tests/editgot',\n" +
            "            success: function (data, textStatus, jqXHR) {\n" +
            "\n" +
            "                $('#updategot').html('Save changes');\n" +
            "                $('#updategot').removeAttr(\"disabled\");"+
            "\n" +
            "            },\n" +
            "            error: function (ts) {\n" +
            "            }\n" +
            "        });\n" +
            "    }"+
            "</script>";

}
