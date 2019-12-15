/**
 * 评论回复
 * */
function post() {
    var questionId = $("#question_id").val();
    var content = $("#comment_content").val();
    comment2target(questionId,1,content);
}
/**
 * 分装回复问题以及评论的js代码
 * */
function comment2target(targetId,type,content) {
    if (!content) {
        alert("不能回复空内容~~~");
        return;
    }
    $.ajax({
        type:"POST",
        url:"/comment",
        contentType:'application/json',
        data:JSON.stringify({
            "parentId":targetId,
            "content":content,
            "type":type
        }),
        success: function (response) {
            if (response.code == 200){
                window.location.reload();
            }else {
                //实现未登录时评论时能够选择跳转到登录页面登录
                if(response.code == 2003){
                    var isAccepted = confirm(response.message);
                    if(isAccepted){
                        window.open("http://github.com/login/oauth/authorize?client_id=f23364e0b3dbb39734fa&redirect_uri=http://localhost:8080/callback&scope=user&state=1");
                        window.localStorage.setItem("closable",true);
                    }
                }else {
                    alert(response.message);
                }
            }
        },
        dataType:"json"
    });
}
function comment(e) {
    var commentId = e.getAttribute("data-id");
    var content = $("#input-"+commentId).val();
    comment2target(commentId,2,content);
}
/**
 * 展开二级评论
 * */
function collapseComments(e) {
    var id = e.getAttribute("data-id");
    var comments = $("#comment-"+id);
    //获取二级评论的展开状态，即设立一个标记
    var collapse = e.getAttribute("data-collapse");
    //根据二级评论的展开状态进行折叠或展开操作
    if(collapse){
        //折叠评论
        comments.removeClass("in");
        //去除标记
        e.removeAttribute("data-collapse");
        //去除css样式
        e.classList.remove("active");
    }else{
        var subCommentContainer = $("#comment-"+id);
        //对二级评论进行判断，避免多次迭代后台获取的数据
        if(subCommentContainer.children().length == 1){
            $.getJSON("/comment/" + id, function (data) {
                $.each(data.data.reverse(), function (index, comment) {
                    var mediaLeftElement = $("<div/>", {
                        "class": "media-left"
                    }).append($("<img/>", {
                        "class": "media-object img-rounded",
                        "src": comment.user.avatarUrl
                    }));

                    var mediaBodyElement = $("<div/>", {
                        "class": "media-body"
                    }).append($("<h5/>", {
                        "class": "media-heading",
                        "html": comment.user.name
                    })).append($("<div/>", {
                        "html": comment.content
                    })).append($("<div/>", {
                        "class": "menu"
                    }).append($("<span/>", {
                        "class": "pull-right",
                        "html": moment(comment.gmtCreate).format('YYYY-MM-DD')
                    })));
                    // moment(comment.gmtCreate).format('YYYY-MM-DD')
                    var mediaElement = $("<div/>", {
                        "class": "media"
                    }).append(mediaLeftElement).append(mediaBodyElement);

                    var commentElement = $("<div/>", {
                        "class": "col-lg-12 col-md-12 col-sm-12 col-xs-12 comments"
                    }).append(mediaElement);

                    subCommentContainer.prepend(commentElement);
                });
                //展开二级评论
                comments.addClass("in");
                // 标记二级评论展开状态
                e.setAttribute("data-collapse", "in");
                e.classList.add("active");
            });
        }
            //展开二级评论
            comments.addClass("in");
            // 标记二级评论展开状态
            e.setAttribute("data-collapse", "in");
            e.classList.add("active");

    }
}
/**
 * 发布页面中tag标签选择框的显示
 * */
function showSelectTag() {
    $("#select-tag").show();
}
/**
 * 点击tag选择框中的标签后将该标签的内容写入input框
 * */
function selectTag(e) {
    var value = e.getAttribute("data-tag");
    var previous = $("#tag").val();
    if(previous.indexOf(value) == -1){
        if(previous){
            $("#tag").val(previous + ","+value);
        }else{
            $("#tag").val(value);
        }
    }
}