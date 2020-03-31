$(document).ready( function () {

    $.ajax({
        type: 'GET',
        dataType: "json",
        url: '/rest/api/renewsession',
        success: function (data, textStatus, jqXHR) {
        },
        error: function (ts) {
        }
    });
});


$( function() {



    function split( val ) {
        return val.split( / \s*/ );
    }
    function extractLast( term ) {
        return split( term ).pop();
    }
    function checkURI( term ) {
        if(term[1] != null)
            return term[1].value;
        else
            return "";
    }

    function checkFile( term ) {
        if(term[0] != null)
            return term[0].value.replace(/><\/http.*:>/g,'\/>');
        else
            return "";
    }


    $( "#test" )
        .keydown( function( event ) {
            if ( event.keyCode === $.ui.keyCode.TAB &&
                $( this ).autocomplete( "instance" ).menu.active ) {
                event.preventDefault();
            }
            syntaxChecker();
        })
        .keypress( function( event ) {
            if ( event.keyCode === $.ui.keyCode.TAB &&
                $( this ).autocomplete( "instance" ).menu.active ) {
                event.preventDefault();
            }
        })
        .autocomplete({
            minLength: 1,
            source: function( request, response ) {

                $.ajax({
                    type: "POST",
                    url:"/rest/api/autocompleteFromUriFile",
                    contentType: "application/json",
                    data: JSON.stringify({
                        test: request.term,
                        lastTerm: extractLast( request.term ),
                        ontologyUri: checkURI(document.getElementsByName("ontology")),
                        code: checkFile(document.getElementsByName("ontologycode")),
                        imports: "true"
                    }),
                    success: response,
                    dataType: 'json'
                });

            },
            focus: function() {
                // prevent value inserted on focus
                return false;
            },
            select: function( event, ui ) {
                var terms = split( this.value );
                // remove the current input
                terms.pop();
                // add the selected item
                terms.push( ui.item.value );
                // add placeholder to get the comma-and-space at the end
                if(ui.item.value == ";"){
                    terms.push(",");
                }
                terms.push( "" );
                this.value = terms.join( " " );
                syntaxChecker();
                return false;
            }
        });
} );

function syntaxChecker(){
    var test = $('#test').val();
    $.ajax({
        type: 'GET',
        dataType: "json",
        data: { test: test},
        url: '/rest/api/syntaxChecker',
        success: function (data, textStatus, jqXHR) {
            var result = data;

            if(result == true){
                $("#test").css('background-color', 'rgba(204, 255, 204, 0.3)');
            }else{
                $("#test").css('background-color', 'rgba(255, 102, 102, 0.3)');
            }
        },
        error: function (data, textStatus, jqXHR) {
        }
    });

}

// ascending order
function sortByLabel(x,y) {
    return x.Label - y.Label;
}

function sortJSON(jsonArray) {
    return jsonArray.sort(sortByLabel);
}


function hideRows(text) {
    index = 1;
    tr = document.getElementById('tr'+index);
    while (tr!=null){
        valores = document.getElementById('inp'+index).value;
        if (valores.indexOf(text)==-1){
            tr.style.display='none';
        }
        index++;
        tr = document.getElementById('tr'+index);
    }
    document.getElementById('remButt').style.display='';
}


function removeCheck(id) {
    $('#notmatch').html('');
    $('#'+id).remove();
    if($('#storedtests').html().trim().length == 0) {
        $('#checksuite').attr("disabled", true)

    }
}

function removeAll(id) {

    var labels = document.getElementsByName("testlabel");
    var report = document.getElementsByName("report");
    report.forEach(function (item) {
        item.style.visibility = 'hidden';
    });

    var len = labels.length;
    parentNode = labels[0].parentNode;
    for(var i=0; i<len; i++)
    {
        parentNode.removeChild(labels[0]);
    }

    var remove = document.getElementById("removeall");
    remove.remove();
    $('#checksuite').attr("disabled", true);

}

function check() {
    $('#checktests').html("<div class=\"loader\">\n" +
        "                        <span></span>\n" +
        "                        <span></span>\n" +
        "                        <span></span>\n" +
        "                    </div>");
    $('#checktests').attr("disabled", true);


    $('#checktests').html("<div class=\"loader\">\n" +
        "                        <span></span>\n" +
        "                        <span></span>\n" +
        "                        <span></span>\n" +
        "                    </div>");
    $('#checktests').attr("disabled", true);
    $('#notmatch').html('');

    var tables =document.getElementsByName('tablegot');

    var myObj ={};
    tables.forEach(function(table) {

        var tableid= table.id;
        //loops through rows
        var myRows = [];
        var $headers = $(table).find("th");
        var $rows = $(table).find("tbody tr").each(function (index) {
            $cells = $(this).find("td.got");

            myRows[index] = {};
            $cells.each(function (cellIndex) {
                var  header = $($headers[cellIndex]).html();
                header = header.replace("<span class=\"glyphicon glyphicon-pencil\"></span>","");
                myRows[index][header] = $(this).html();
            });
        });

        myObj[table.id] = myRows;
    });

    var array = $(this).serializeArray();
    var tests = document.getElementsByName("test")[0].value.split(";");

    var arrayontosId = [];
    var idontos = document.getElementsByName("ontology");
    idontos.forEach(function (item) {
        if(item.value!="") {
            arrayontosId.push(item.value);
        }
    });

    var arrayontosCode = [];
    var arrayontosCodeTextArea = document.getElementsByName("ontologycode");
    arrayontosCodeTextArea.forEach(function (item) {
        if(item.value!="") {
            var value =item.value;
            var ontologyCode = value.replace(/><\/http.*:>/g,'\/>');
            arrayontosCode.push(ontologyCode);
        }
    });

    var data = {
        got: JSON.stringify(myObj),
        ontologies:arrayontosId,
        ontologiesCode:arrayontosCode,
        tests: tests
    };

    $.ajax({
        type: 'POST',
        data: JSON.stringify(data),
        dataType: "json",
        url: '/rest/api/results',
        contentType: "application/json",
        success: function (data, textStatus, jqXHR) {
            if (data.length > 0) {
                $.each(data, function (i, item) {
                    var table = document.getElementById("table");
                    if(item.Results.length <= 1 && table != null){
                        $.each(item.Results, function (i, result) {
                            var table = document.getElementById("table");
                            var row = table.insertRow(1);
                            var cell1 = row.insertCell(0);
                            var cell2 = row.insertCell(1);
                            var cell3 = row.insertCell(2);
                            var cell4 = row.insertCell(3);
                            cell4.innerHTML = "<button type=\"button\" class=\"btn btn-default\" onclick=\"removeOntology(this)\" title=\"Remove test\"> <span class=\"submit glyphicon glyphicon-remove-sign\"    style =\"color:rgb(255, 0, 0)\"></span> </button>";
                            if (result.Result == 'Passed') {
                                cell1.innerHTML = "<p name=\"testintable\">" + item.Test + "</p>";
                                cell2.innerHTML = "<span class=\"label label-success\" data-toggle=\"tooltip\" title=\"The ontology passed the test\">Passed</span>";
                                cell3.innerHTML = "<p>None</p>";
                            } else if (result.Result == 'Undefined') {
                                var test = item.Test;

                                $.each(result.Undefined, function (j, undefined) {
                                    test = test.replace(undefined, "<span style=\"color:red;\">"+undefined+"</span>");
                                });

                                cell1.innerHTML = "<p name=\"testintable\">" + test + "</p>";

                                cell2.innerHTML = "<span class=\"label label-default \" data-toggle=\"tooltip\" title=\"The ontology did not pass the test\">Undefined terms</span>";
                                cell3.innerHTML = "<p>The terms in the test are not correctly defined in the ontology</p>";

                            }else if(result.Result == 'Incorrect'){
                                var test = item.Test;
                                $.each(result.Incorrect, function (j, incorrect) {
                                    test = test.replace(incorrect, "<span style=\"color:#ff7f50;\">" + incorrect + "</span>");
                                });
                                cell1.innerHTML = "<p name=\"testintable\">" + test + "</p>";

                                cell2.innerHTML = "<span class=\"label label-default \" data-toggle=\"tooltip\" title=\"The ontology did not pass the test\">Undefined terms</span>";
                                cell3.innerHTML = "<p>The terms in the test are not correctly defined in the ontology</p>";
                            } else if (result.Result == 'Absent') {
                                cell1.innerHTML = "<p name=\"testintable\">" + item.Test + "</p>";

                                cell2.innerHTML = "<span class=\"label label-warning\" data-toggle=\"tooltip\" title=\"The ontology did not pass the test\">Absent relation</span>";
                                cell3.innerHTML = "<p>The ontology does not implement the requirement associated to the test</p>";
                            } else {
                                cell1.innerHTML = "<p name=\"testintable\">" + item.Test + "</p>";

                                cell2.innerHTML = "<span class=\"label label-danger\" data-toggle=\"tooltip\" title=\"The ontology did not pass the test\">Conflict</span>";
                                cell3.innerHTML = "<p>The ontology has a relation which causes a conflict with the one define in the test</p>";
                            }
                        });
                    }else if(item.Results.length <= 1 && table == null) {

                        var table = document.getElementById("tablemultiple");
                        //  var table = document.getElementsByName("tableresults")[0].setAttribute("id","tablemultiple");
                        document.getElementsByName("tableresults")[0].setAttribute("id","table");
                        var table = document.getElementById("table");
                        //document.getElementsByTagName("table")[0].setAttribute("id","table");
                        //var table = document.getElementById("table");

                        $("#table").html("");

                        var header="<thead>\n"+
                            "\t<tr>\n" +
                            "\t\t<th>Test</th>\n"+
                            "\t\t<th>Result</th>\n" +
                            "\t\t<th>Problem</th>\n"+
                            "<th></th>\n"+
                            "\t</tr>\n"+
                            "</thead>";

                        $("#table").append(header);


                        $.each(item.Results, function (i, result) {
                            var row = table.insertRow(1);
                            var cell1 = row.insertCell(0);
                            var cell2 = row.insertCell(1);
                            var cell3 = row.insertCell(2);
                            var cell4 = row.insertCell(3);
                            cell1.innerHTML = "<p name=\"testintable\">" + item.Test + "</p>";
                            cell4.innerHTML = "<button type=\"button\" class=\"btn btn-default\" onclick=\"removeOntology(this)\" title=\"Remove test\"> <span class=\"submit glyphicon glyphicon-remove-sign\"    style =\"color:rgb(255, 0, 0)\"></span> </button>";
                            if (result.Result == 'passed') {
                                cell2.innerHTML = "<span class=\"label label-success\" data-toggle=\"tooltip\" title=\"The ontology passed the test\">Passed</span>";
                                cell3.innerHTML = "<p>None</p>";
                            } else if (result.Result == 'undefined') {
                                var test = item.Test;
                                $.each(result.Undefined, function (j, undefined) {
                                    test = test.replace(undefined, "<span style=\"color:red;\">"+undefined+"</span>");
                                });
                                cell1.innerHTML = "<p name=\"testintable\">" + test + "</p>";
                                cell2.innerHTML = "<span class=\"label label-default \" data-toggle=\"tooltip\" title=\"The ontology did not pass the test\">Undefined terms</span>";
                                cell3.innerHTML = "<p>The terms in the test are not defined in the ontology</p>";

                                cell2.innerHTML = "<span class=\"label label-default \" data-toggle=\"tooltip\" title=\"The ontology did not pass the test\">Undefined terms</span>";
                                cell3.innerHTML = "<p>The terms in the test are not defined in the ontology</p>";
                            } else if (result.Result == 'absent') {
                                cell2.innerHTML = "<span class=\"label label-warning\" data-toggle=\"tooltip\" title=\"The ontology did not pass the test\">Absent relation</span>";
                                cell3.innerHTML = "<p>The ontology does not implement the requirement associated to the test</p>";
                            } else {
                                cell2.innerHTML = "<span class=\"label label-danger\" data-toggle=\"tooltip\" title=\"The ontology did not pass the test\">Conflict</span>";
                                cell3.innerHTML = "<p>The ontology has a relation which causes a conflict with the one define in the test</p>";
                            }
                        });

                    }else if(item.Results.length > 1 && table != null){

                        // document.getElementsByTagName("table")[0].setAttribute("id","tablemultiple"); //cambiar esto?
                        document.getElementById("table").setAttribute("id","tablemultiple");
                        var table = document.getElementById("tablemultiple");
                        $("#tablemultiple").html("");

                        var header="<thead>\n"+
                            "\t<tr>\n" +
                            "\t\t<th>Test</th>\n";

                        item.Results.forEach(function (ontology) {
                            header+="<th>"+ontology.Ontology+"</th>\n";
                        });
                        header+="<th></th>\n";
                        header+="\t</tr>\n"+
                            "</thead>";

                        $("#tablemultiple").append(header);


                        var table = "<tbody>";
                        table +="<td  class=\"col\">"+item.Test+"</td>";
                        item.Results.forEach(function (result) {
                            if (result.Result == 'passed') {
                                table +="<td  class=\"col\"><span class=\"label label-success\" data-toggle=\"tooltip\" title=\"The ontology passed the test\">Passed</span></td>";
                            } else if (result.Result == 'undefined') {
                                table += "<td  class=\"col\"><span class=\"label label-default \" data-toggle=\"tooltip\" title=\""+result.Undefined+" not defined in the ontology\">Undefined terms</span></td>";
                            } else if (result.Result == 'absent') {
                                table += "<td  class=\"col\"><span class=\"label label-warning\" data-toggle=\"tooltip\" title=\"The ontology did not pass the test\">Absent relation</span></td>";
                            } else {
                                table += "<td  class=\"col\"><span class=\"label label-danger\" data-toggle=\"tooltip\" title=\"The ontology did not pass the test\">Conflict</span></td>";
                            }
                        });
                        table +="<td  class=\"col\"><button type=\"button\" class=\"btn btn-default\" onclick=\"removeOntology(this)\" title=\"Remove test\"> <span class=\"submit glyphicon glyphicon-remove-sign\"    style =\"color:rgb(255, 0, 0)\"></span> </button></td></tbody>";
                        $("#tablemultiple").append(table);
                    }else{

                        var table = document.getElementById("tablemultiple");

                        var row = table.insertRow(1);
                        var cell2 = row.insertCell(0);
                        cell2.innerHTML = "<td  class=\"col\">"+item.Test+"</td>";
                        var index=1;
                        $.each(item.Results, function (i, result) {
                            var cell1 = row.insertCell(i+1);
                            if (result.Result == 'passed') {
                                cell1.innerHTML ="<td  class=\"col\"><span class=\"label label-success\" data-toggle=\"tooltip\" title=\"The ontology passed the test\">Passed</span></td>";
                            } else if (result.Result == 'undefined') {

                                cell1.innerHTML ="<td  class=\"col\"><span class=\"label label-default \" data-toggle=\"tooltip\" title=\""+result.Undefined+"  not defined in the ontology\">Undefined terms</span></td>";
                            } else if (result.Result == 'absent') {
                                cell1.innerHTML ="<td  class=\"col\"><span class=\"label label-warning\" data-toggle=\"tooltip\" title=\"The ontology did not pass the test\">Absent relation</span></td>";
                            } else {
                                cell1.innerHTML = "<td  class=\"col\"><span class=\"label label-danger\" data-toggle=\"tooltip\" title=\"The ontology did not pass the test\">Conflict</span></td>";
                            }
                            index++;


                        });
                        var cell3 = row.insertCell(index);
                        cell3.innerHTML = "<td class=\"col\"></td><button type=\"button\" class=\"btn btn-default\" onclick=\"removeOntology(this)\" title=\"Remove test\"> <span class=\"submit glyphicon glyphicon-remove-sign\"    style =\"color:rgb(255, 0, 0)\"></span> </button></td>";


                    }
                });
            } else {

                $('#notmatch').html("<font color=\"red\">This is not a test expression supported by Themis</font>");

            }
            $('#checktests').html('Check');
            $('#checktests').removeAttr("disabled");
        },
        error: function (data, textStatus, jqXHR) {
            $('#checktests').html('Check');
            $('#checktests').removeAttr("disabled");

            $('#notmatch').html("<font color=\"red\">Something went wrong. Check that the ontology doesn't have any inconsistencies or unsatisfiable classes</font>");
            $('#' + idrandom).remove();

        }
    });

}

function hover(but) {  //Inicia la edición de una fila
    var color = $(but).css('background-color');
    if(color == "rgb(220, 220, 220)" )
        $(but).css('background-color', "white");
    else
        $(but).css('background-color', "gainsboro");
}

function rowEdit(but) {  //Inicia la edición de una fila
    var $row = $(but).parents('tr');  //accede a la fila
    $row.find("td:nth-child(1)").each(function(){
        $(this).attr("contentEditable","true");
        var html = $(this).html();
        var input = $('<input type="text" />');
        input.val(html);
        $(this).html(input);
    });

    $row.find('td:nth-child(3)').css("display","none");
    $row.find('td:nth-child(4)').css("display","block");
}

function rowSave(but) {  //Inicia la edición de una fila
    var html;
    var $row = $(but).parents('tr');  //accede a la fila
    $row.find("td:nth-child(1) input").each(function(){
        html = $(this).val();
        $(this).remove();
    });

    $row.find("td:nth-child(1)").each(function () {
        $(this).html(html);
    });


    var $row = $(but).parents('tr');  //accede a la fila
    $row.find('td:nth-child(1)').attr("contentEditable","false");
    $row.find('td:nth-child(3)').css("display","block");
    $row.find('td:nth-child(4)').css("display","none");
}

function loadontologyFromURI() {
    var array = $(this).serializeArray();
    var id = document.getElementsByName("ontology");
    id.forEach(function (item) {
        var test = document.getElementsByName(item.value);
        if(item.value!="" && test.length == 0 ) {
            array.push(item.value);

        }
    });

    array.forEach(function (uri) {
        $('#load').html("<div class=\"loader\">\n" +
            "                        <span></span>\n" +
            "                        <span></span>\n" +
            "                        <span></span>\n" +
            "                    </div>");
        $('#load').attr("disabled", true);
        var exists = document.getElementsByName(uri);
        if(uri!="" && exists.length ==0) {
            $.ajax({
                type: 'POST',
                dataType: "json",
                data: JSON.stringify(uri),
                url: '/rest/api/gotAsTableFromURI',
                success: function (data, textStatus, jqXHR) {
                    //  var text = "<p   name=\"" + uri + "\"><a href=\"" + uri + "\"><button  type=\"button\" class=\"btn btn-link\" ><small><span class=\"glyphicon glyphicon-ok align-middle\" aria-hidden=\"true\"></span></small></button></a></p>";
                    // $("#loadcheck").append(text);

                    var text = "<p   name=\"" + uri + "\"><a href=\"" + uri + "\"><button  data-toggle=\"collapse\" name=\"ontology\" type=\"button\" class=\"btn btn-link\" value='" + uri + "'><small><span class=\"glyphicon glyphicon-ok align-middle\" aria-hidden=\"true\"></span> " + uri + "</small></button></a></p>";
                    $("#loadonto").append(text);


                    var text = "<p   class= \"collapse-title\" name=\"" + uri + "\"><a><button data-toggle=\"collapse\" type=\"button\" data-target=\"#" + data.key + "collapse\" aria-expanded=\"false\"  aria-controls=\"" + data.key + "collapse\" class=\"btn btn-link\" id=\"" + uri + "\" onclick='hover(this)' '><span title=\"See got \" class=\"glyphicon glyphicon-chevron-down align-middle\" aria-hidden=\"true\"></span><small> See the glossary of terms</small> </button></a></p>";
                    $("#loadgot").append(text);

                    var text2 = "           <div  name=\"" + uri + "\" style = \"background-color: gainsboro\" class=\"collapse col-md-12\" id=\"" + data.key + "collapse\">\n" +
                        "                            <div class=\"card card-body\">\n" + data.got +

                        "                            </div>\n" +
                        "                        </div>";


                    $("#aux").append(text2);

                    var text = "<p   name=\"" + uri + "\"><a><button type=\"button\" class=\"btn btn-link\" onclick=\"removeGot('" + uri + "')\" ><small><span title=\"Remove ontology\" class=\"glyphicon glyphicon-remove align-middle\" aria-hidden=\"true\"></span> Remove</small> </button></a></p>";
                    $("#deleteonto").append(text);



                    document.getElementById("ontology").value ="";
                    $('#load').html('Load ontology');
                    $('#load').removeAttr("disabled");
                    $('#checktests').removeAttr("disabled");
                    $('#export').removeAttr("disabled");
                    $('#loadtest').removeAttr("disabled");
                    var testsch = document.getElementById("checkout-tests");
                    testsch.style.opacity=1;


                },
                error: function (ts) {
                    document.getElementById("ontology").value ="";
                    $('#load').html('Load ontology');
                    $('#load').removeAttr("disabled");
                    $('#checktests').removeAttr("disabled");
                    $('#export').removeAttr("disabled");
                    $('#loadtest').removeAttr("disabled");
                    var testsch = document.getElementById("checkout-tests");
                    testsch.style.opacity=1;
                }
            });




        }



    });


}



function loadontologyFromFile(){
    var array = $(this).serializeArray();
    var id = document.getElementsByName("ontologyfile");
    id.forEach(function (item) {
        var test = document.getElementsByName(item.value);
        if(item.value!="" && test.length == 0 ) {
            array.push(item.value);

        }
    });


    array.forEach(function (ontologyfile) {
        $('#loadfile').html("<div class=\"loader\">\n" +
            "                        <span></span>\n" +
            "                        <span></span>\n" +
            "                        <span></span>\n" +
            "                    </div>");
        $('#loadfile').attr("disabled", true);
        var exists = document.getElementsByName(ontologyfile);
        var ontologyCode = ontologyfile.replace(/><\/http.*:>/g,'\/>');
        if(ontologyfile!="" && exists.length ==0) {
            $.ajax({
                type: 'POST',
                dataType: "json",
                data: ontologyCode,
                url: '/rest/api/gotAsTableFromFile',
                success: function (data, textStatus, jqXHR) {

                    //  var text = "<p   name=\"" + uri + "\"><a href=\"" + uri + "\"><button  type=\"button\" class=\"btn btn-link\" ><small><span class=\"glyphicon glyphicon-ok align-middle\" aria-hidden=\"true\"></span></small></button></a></p>";
                    // $("#loadcheck").append(text);
                    var text = "<p   name=\"" + data.uri + "\"><button  data-toggle=\"collapse\" name=\"ontologyfile\" type=\"button\" class=\"btn btn-link\" value='" + data.uri + "'><small><span class=\"glyphicon glyphicon-ok align-middle\" aria-hidden=\"true\"></span> " + data.uri + "</small></button></p>";


                    text+="<div  name=\"" + data.uri + "\" ><textarea  style=\"display:none;\" name = \"ontologycode\" id=\""+data.key+"\">"+ontologyfile.replace("/><\/https:>/g",'\/>')+"</textarea></div>";

                    $("#loadonto").append(text);

                    var text = "<p   class= \"collapse-title\" name=\"" + data.uri + "\"><a><button data-toggle=\"collapse\" type=\"button\" data-target=\"#" + data.key + "collapse\" aria-expanded=\"false\"  aria-controls=\"" + data.key + "collapse\" class=\"btn btn-link\" id=\"" + data.uri + "\" onclick='hover(this)' '><span title=\"See got \" class=\"glyphicon glyphicon-chevron-down align-middle\" aria-hidden=\"true\"></span><small> See the glossary of terms</small> </button></a></p>";
                    $("#loadgot").append(text);

                    var text2 = "           <div  name=\"" + data.uri + "\" style = \"background-color: gainsboro\" class=\"collapse col-md-12\" id=\"" + data.key + "collapse\">\n" +
                        "                            <div class=\"card card-body\">\n" + data.got +

                        "                            </div>\n" +
                        "                        </div>";


                    $("#aux").append(text2);

                    var text = "<p   name=\"" + data.uri + "\"><a><button type=\"button\" class=\"btn btn-link\" onclick=\"removeGot('" + data.uri + "')\" ><small><span title=\"Remove ontology\" class=\"glyphicon glyphicon-remove align-middle\" aria-hidden=\"true\"></span> Remove</small> </button></a></p>";
                    $("#deleteonto").append(text);


                    document.getElementById("ontologyfile").value ="";
                    $('#loadfile').html('Load ontology from file');
                    $('#loadfile').removeAttr("disabled");
                    $('#checktests').removeAttr("disabled");
                    $('#export').removeAttr("disabled");
                    $('#loadtest').removeAttr("disabled");
                    var testsch = document.getElementById("checkout-tests");
                    testsch.style.opacity=1;


                },
                error: function (ts) {
                    document.getElementById("ontologyfile").value ="";
                    $('#loadfile').html('Load ontology from file');
                    $('#loadfile').removeAttr("disabled");
                    $('#checktests').removeAttr("disabled");
                    $('#export').removeAttr("disabled");
                    $('#loadtest').removeAttr("disabled");
                    var testsch = document.getElementById("checkout-tests");
                    testsch.style.opacity=1;
                }
            });




        }



    });
}

function openTests() {
    window.open('tests-info.html', '_blank');
}


function removeGot(uri) {
    var elements = document.getElementsByName(uri);
    while(elements.length > 0){
        elements[0].parentNode.removeChild(elements[0]);
    }
}

function loadTests() {

    var id = document.getElementById("testuri");
    if(id.value!= null && id.value!="") {
        $('#loadtest').html("<div class=\"loader\">\n" +
            "                        <span></span>\n" +
            "                        <span></span>\n" +
            "                        <span></span>\n" +
            "                    </div>");

        $('#loadtest').attr("disabled", true);
        $('#checktests').attr("disabled", true);
        $('#export').attr("disabled", true);
        $.ajax({
            type: 'POST',
            data: JSON.stringify(id.value),
            dataType: "json",
            url: '/rest/api/loadTests',
            success: function (data, textStatus, jqXHR) {
                if (data.length > 0) {
                    var tests = "";
                    $.each(data, function (i, item) {
                        tests += item.Test + ";\n";
                    });
                    $('#test').val('');
                    $('#test').val(tests);
                    $('#loadtest').html('Load test');
                    $('#loadtest').removeAttr("disabled");
                    $('#checktests').removeAttr("disabled");
                    $('#export').removeAttr("disabled");
                } else {

                    $('#notmatch').html("This is not a test expression");

                }

            },
            error: function (ts) {

            }
        });
    }
}

function loadTestsFromFile() {

    var id = document.getElementById("testfile");
    if(id.value!= null && id.value!="") {
        $('#loadtestfile').html("<div class=\"loader\">\n" +
            "                        <span></span>\n" +
            "                        <span></span>\n" +
            "                        <span></span>\n" +
            "                    </div>");

        $('#loadtestfile').attr("disabled", true);
        $('#checktests').attr("disabled", true);
        $('#export').attr("disabled", true);
        var ontologyCode = id.value.replace(/><\/http.*:>/g,'\/>');
        $.ajax({
            type: 'POST',
            data: ontologyCode.replace('/#/g',''),
            dataType: "json",
            url: '/rest/api/loadTestsFromFile',
            success: function (data, textStatus, jqXHR) {
                if (data.length > 0) {
                    var tests = "";
                    $.each(data, function (i, item) {
                        tests += item.Test + ";\n";
                    });
                    document.getElementById('testfile').innerHTML = '';
                    $('#test').val('');
                    $('#test').val(tests);

                    $('#loadtestfile').html('Load tests from file');
                    $('#loadtestfile').removeAttr("disabled");
                    $('#checktests').removeAttr("disabled");
                    $('#export').removeAttr("disabled");
                } else {

                    $('#notmatch').html("This is not a test expression");

                }

            },
            error: function (ts) {

            }
        });
    }
}

function  removeOntology(btn) {

    var row = btn.parentNode.parentNode;
    row.parentNode.removeChild(row);

}


function exportfile(){
    var array = $(this).serializeArray();
    var id = document.getElementsByName("testintable");

    id.forEach(function (item) {
        if(item.value!="") {
            array.push(item.innerHTML);
        }
    });

    window.location="/rest/api/export?test="+array;

}

function showRows() {
    index = 1;
    tr = document.getElementById('tr'+index);
    while (tr!=null){
        tr.style.display='';
        index++;
        tr = document.getElementById('tr'+index);
    }
    butt = document.getElementById('remButt').style.display='none';
}

