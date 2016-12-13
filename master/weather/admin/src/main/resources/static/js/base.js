/**
 * Created by lingzhiyuan on 16/4/18.
 */

// 更改缓存的当前省, 当前市, 当前区
var changeCurrentCityById = function(cityId) {
    window.currentCity = findCityById(cityId);
    if(window.currentCity == null){
        alert("更改失败, 未找到市")
    }else{
        sessionStorage.setItem("currentCity", window.currentCity);
        return true
    }
};

var changeCurrentProvinceById = function(provinceId) {
    window.currentProvince = findProvinceById(provinceId);
    if(window.currentProvince == null){
        alert("更改失败, 未找到省")
    }else{
        sessionStorage.setItem("currentProvince",window.currentProvince) ;
        return true;
    }
};

var changeCurrentDistrictById = function(districtId) {
    window.currentDistrict = findDistrictById(districtId);
    if(window.currentDistrict == null){
        alert("更改失败, 未找到区县")
    }else{
        localStorage.setItem("currentDistrict", JSON.stringify(window.currentDistrict));
    }
};

$(function(){

    // 设置 moment.js 的默认区域为中国
    moment.locale("zh-cn")

    // 从 sessionStorage 中获取区域信息, 若未获取到则从服务器获取再保存
    window.provinces = JSON.parse(sessionStorage.getItem("provinces"))
    if(window.provinces == null || window.provinces.length == 0) {
        window.provinces = getProvinces("location/provinces")
        if(window.provinces.length != 0){
            sessionStorage.setItem("provinces", JSON.stringify(window.provinces))
        }
    }

    window.cities = JSON.parse(sessionStorage.getItem("cities"))
    if(window.cities == null || window.cities.length == 0){
        window.cities = getCities("location/cities")
        if(window.cities.length != 0) {
            sessionStorage.setItem("cities", JSON.stringify(window.cities))
        }
    }

    window.districts = JSON.parse(sessionStorage.getItem("districts"))
    if(window.districts == null || window.districts.length == 0){
        window.districts = getDistricts("location/districts")
        if(window.districts.length != 0){
            sessionStorage.setItem("districts", JSON.stringify(window.districts))
        }
    }

    // 当前区县需要长期保存
    window.currentDistrict = JSON.parse(localStorage.getItem("currentDistrict"))
    if(window.currentDistrict == null)
    {
        // 设置默认地点, 北京
        changeCurrentDistrictById(101010100)
    }

    // 获取天气编码
    getCodes("/static/json/cnweather.json")

});

