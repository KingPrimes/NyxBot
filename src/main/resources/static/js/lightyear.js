var lightyear = function(){
	
	/**
	 * 页面loading
	 */
	var pageLoader = function($mode) {
		var $loadingEl = jQuery('#lyear-loading');
		    $mode      = $mode || 'show';
		if ($mode === 'show') {
			if ($loadingEl.length) {
				$loadingEl.fadeIn(250);
			} else {
				jQuery('body').prepend('<div id="lyear-loading"><div class="spinner-border text-primary" role="status"><span class="sr-only">Loading...</span></div></div>');
			}
		} else if ($mode === 'hide') {
			if ($loadingEl.length) {
				$loadingEl.fadeOut(250);
			}
		}
		return false;
	};
	
    /**
     * 页面小提示
     * @param $msg 提示信息
     * @param $type 提示类型:'info', 'success', 'warning', 'danger'
     * @param $icon 图标，例如：'fa fa-user' 或 'glyphicon glyphicon-warning-sign'
     * @param $from 'top' 或 'bottom'
     * @param $align 'left', 'right', 'center'
     * @author CaiWeiMing <314013107@qq.com>
     */
    var tips = function ($msg, $type, $icon, $from, $align) {
        $type  = $type || 'info';
        $from  = $from || 'top';
        $align = $align || 'center';
        $enter = $type == 'danger' ? 'animated shake' : 'animated fadeInUp';

        jQuery.notify({
            icon: $icon,
            message: $msg
        },
        {
            element: 'body',
            type: $type,
            allow_dismiss: true,
            newest_on_top: true,
            showProgressbar: false,
            placement: {
                from: $from,
                align: $align
            },
            offset: 20,
            spacing: 10,
            z_index: 10800,
            delay: 3000,
            timer: 1000,
            animate: {
                enter: $enter,
                exit: 'animated fadeOutDown'
            }
        });
    };
	
	return {
        // 页面小提示
        notify  : function ($msg, $type, $icon, $from, $align) {
            tips($msg, $type, $icon, $from, $align);
        },
        // 页面加载动画
		loading : function ($mode) {
		    pageLoader($mode);
		}
    };
}();