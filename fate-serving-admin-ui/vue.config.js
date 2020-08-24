/**
*  Copyright 2019 The FATE Authors. All Rights Reserved.
*
*  Licensed under the Apache License, Version 2.0 (the 'License');
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an 'AS IS' BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*
**/
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer')
    .BundleAnalyzerPlugin
const path = require('path')
const webpack = require('webpack')
const AddAssetHtmlPlugin = require('add-asset-html-webpack-plugin')
// 导入compression-webpack-plugin
const CompressionWebpackPlugin = require('compression-webpack-plugin')
// 定义压缩文件类型
const productionGzipExtensions = ['js', 'css']

function resolve(dir) {
    return path.join(__dirname, dir)
}
// console.log('==>>proxyTarget==>>', proxyTarget)
let publicPath = process.env.NODE_ENV === 'production' ? './' : '/'
let dllPublishPath = './vendor/'
let IS_PROD = ['production', 'test'].includes(process.env.NODE_ENV)

module.exports = {
    publicPath: publicPath,
    outputDir: 'target/dist',
    lintOnSave: true,
    transpileDependencies: [
        /* string or regex */
        // /[/\\]node_modules[/\\]echarts[/\\]/
    ],
    productionSourceMap: false,
    chainWebpack: config => {
        config.plugins.delete('prefetch')
        config.module
            .rule('svgIcon')
            .test(/\.svg$/)
            .include.add(resolve('src/icons'))
            .end()
            .use('svg-sprite-loader')
            .loader('svg-sprite-loader')
            .tap(options => {
                options = {
                    symbolId: 'icon-[name]'
                }
                return options
            })

        config.module
            .rule('svg')
            .exclude.add(resolve('src/icons'))
            .end()
    },
    css: {
        extract: IS_PROD,
        sourceMap: false,
        loaderOptions: {},
        modules: false
    },
    parallel: require('os').cpus().length > 1,
    pwa: {},
    devServer: {
        disableHostCheck: true,
        open: process.platform === 'darwin',
        host: 'localhost',
        port: 8010,
        https: false,
        hotOnly: false,
        // eslint-disable-next-line no-dupe-keys
        open: true,
        proxy: {},
        before: app => { }
    },
    configureWebpack: config => {
        if (process.env.NODE_ENV === 'production') {
            config.plugins.push(
                new webpack.DllReferencePlugin({
                    context: process.cwd(),
                    manifest: require('./public/vendor/vendor-manifest.json')
                }),
                new AddAssetHtmlPlugin({
                    filepath: path.resolve(__dirname, './public/vendor/*.js'),
                    publicPath: dllPublishPath,
                    outputPath: './vendor'
                }),
                new CompressionWebpackPlugin({
                    filename: '[path].gz[query]',
                    algorithm: 'gzip',
                    test: new RegExp(
                        '\\.(' + productionGzipExtensions.join('|') + ')$'
                    ),
                    threshold: 10240,
                    minRatio: 0.8
                })
            )
            if (process.env.npm_lifecycle_event === 'analyze') {
                config.plugins.push(new BundleAnalyzerPlugin())
            }
        } else {
        }
    },
    pluginOptions: {}
}
