function setupQuestionManagement() {
    var editor;
    $("#deleteQuestionsConfirm").on("click", function () {
        $.ajax({
            url: "/player/admin/deleteAllQuestions",
            type: "GET",
            success: function () {
                $('#tab-content').load('/player/admin/questionManagement');
            }
        });
    });

    $("#saveButton").on("click", function () {
        $.ajax({
            url: "/player/admin/saveQuestions",
            type: "PUT",
            data: JSON.stringify(
                editor.get()
            ),
            contentType: "application/json"
        });
    });

    $.ajax({
        url: '/questions/getJson',
        type: 'GET',
        success: function (data) {
            let json = JSON.parse(data);
            const element = document.getElementById('jsonEditorElement');
            const options = {}
            editor = new JSONEditor(element, options)
            editor.set(json)
        },
        error: function (error) {
            console.log(error);
        }
    });
}