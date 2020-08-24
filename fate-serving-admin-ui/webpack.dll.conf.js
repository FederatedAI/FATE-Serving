const path = require('path')
const webpack = require('webpack')
const CleanWebpackPlugin = require('clean-webpack-plugin')

const dllPath = 'public/vendor'

module.exports = {
    entry: {
        vendor: ['vue', 'vue-router', 'vuex', 'axios', 'element-ui']
    },
    output: {
        path: path.join(__dirname, dllPath),
        filename: `[name].dll.${Math.ceil(Math.random() * 10000)}.js`,
        library: '[name]_[hash]'
    },
    plugins: [
        new CleanWebpackPlugin(['*.*'], {
            root: path.join(__dirname, dllPath)
        }),
        new webpack.DefinePlugin({
            'process.env': {
                NODE_ENV: 'production'
            }
        }),
        new webpack.DllPlugin({
            path: path.join(__dirname, dllPath, '[name]-manifest.json'),
            name: '[name]_[hash]',
            context: process.cwd()
        })
    ]
}
