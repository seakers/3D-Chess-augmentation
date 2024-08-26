





import dash
import dash_bootstrap_components as dbc
from   flask                     import Flask, render_template, url_for, request, jsonify, redirect






tradespace_analysis_tool = Flask(__name__, static_folder='/flask/static', static_url_path="/flask/static", template_folder='/flask/template')

app = dash.Dash(__name__, external_stylesheets=[dbc.themes.BOOTSTRAP], server=tradespace_analysis_tool, url_base_pathname='/data/', assets_url_path='/flask/assets')
app.title = "TAT-C"
app.config['suppress_callback_exceptions']=True







