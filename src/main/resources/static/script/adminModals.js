function setupUserEvents() {
    $("#deleteUserAdminModal").on('show.bs.modal', function (event) {
        let button = $(event.relatedTarget);
        let username = button.attr('data-bs-username');
        $(".modal-title b").text('"' + username + '"');
        $("#deleteModalConfirm").attr('data-bs-username', username);
    });

    $("#deleteModalConfirm").click(function () {
        let username = $(this).attr('data-bs-username');
        $.ajax({
            url: "/player/admin/deleteUser",
            type: "GET",
            data: {
                username: username
            },
            success: function (data) {
                $('#tab-content').load('/player/admin/userManagement');
                $("#deleteUserAdminModal").modal('hide');
            }
        });
    });

    $("#changePasswordAdminModal").on('show.bs.modal', function (event) {
        let button = $(event.relatedTarget);
        let username = button.attr('data-bs-username');
        $(".modal-title b").text('"' + username + '"');
        $("#changePasswordConfirm").attr('data-bs-username', username);
    });

    $("#changePasswordConfirm").click(function () {
        let username = $(this).attr('data-bs-username');
        let newPass = $("#changePasswordInput").val();
        $.ajax({
            url: "/player/admin/changePassword",
            type: "GET",
            data: {
                username: username,
                password: newPass
            },
            success: function (data) {
                $('#tab-content').load('/player/admin/userManagement');
                $("#changePasswordAdminModal").modal('hide');
            }
        });
    });

    $("#changeRolesAdminModal").on('show.bs.modal', function (event) {
        let button = $(event.relatedTarget);
        let username = button.attr('data-bs-username');
        $(".modal-title b").text('"' + username + '"');
        $("#changeRolesConfirm").attr('data-bs-username', username);
        $.ajax({
            url: "/player/admin/getRoles",
            type: "GET",
            data: {
                username: username
            },
            success: function (data) {
                let roles = JSON.parse(data);
                let rolesContainer = $("#rolesContainer");
                rolesContainer.empty();
                let i = 0;
                for (const role in roles) {
                    let hasRole = roles[role];
                    let div = $('<div></div>');
                    let input = $('<input type="checkbox" value="' + role + '" id="checkbox' + (i+1) + '"' + (hasRole === true ? 'checked' : '') + '>');
                    let label = $('<label for="checkbox' + (i+1) + '">' + role + '</label>');
                    div.append(input);
                    div.append(label);
                    rolesContainer.append(div);
                    i = i + 1;
                }
            },
            error: function (data) {
                alert("Error: " + data);
            }
        });
    });

    $("#changeRolesConfirm").click(function () {
        let username = $(this).attr('data-bs-username');

        let allRoles = $("#rolesContainer input");
        let roles = {};
        allRoles.each(function() {
            roles[$(this).val()] = $(this).is(':checked');
        });
        let newRoleInput = $("#newRole").val();
        if (newRoleInput !== "") {
            roles[newRoleInput] = true;
        }

        let rolesString = JSON.stringify(roles);

        $.ajax({
            url: "/player/admin/changeRoles",
            type: "GET",
            data: {
                username: username,
                roles: rolesString
            },
            success: function (data) {
                $('#tab-content').load('/player/admin/userManagement');
                $("#changeRolesAdminModal").modal('hide');
            },
            error: function (data) {
                alert("Error: " + data);
            }
        });
    });
}