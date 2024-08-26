from flask import Flask, render_template, url_for, request, jsonify, redirect
from dash.dependencies import Input, Output, State
from collections import OrderedDict

import os
import sys
import socket
import json
import pprint
import pandas               as pd
import pygmo                as pg
import plotly.graph_objs    as go
import numpy                as np

import dash
import dash_table
import dash_core_components as dcc
import dash_html_components as html
import dash_bootstrap_components as dbc

from .PlotClasses import ArchitectureInterface, MissionInterface, TradespaceInterface
from app import app

mapbox_access_token = 'pk.eyJ1IjoiYXBhemFnYWIiLCJhIjoiY2p4ejBvYmE1MDBmcTNjcWs0OTc5a2VybSJ9._c2nW3lIjsaM8jyV75GFtw'

ti = TradespaceInterface('/mission')
interface_input_padding = {'padding-left': '18px'}
analysis_tab_style = {'height': '40px', 'text-align': 'center', 'padding-top': '7px', 'padding-bottom': '7px'}

layout = html.Div([
    dbc.Container([
        dbc.Row([

            # COLUMN HOLDING RADIO BUTTONS
            dbc.Col([
                html.Div([

                    # RADIO BUTTONS
                    dbc.FormGroup(
                        [
                            dbc.Label("Architectures", className='analysis-sats-label'),
                            dbc.RadioItems(
                                options=[
                                    {'label': i.get_name(), 'value': i.get_name()} for i in ti.arch_interfaces
                                ],
                                id='architecture-radio',
                                className='analysis-sat-radio-class',
                                labelClassName='analysis-sat-radio-label',
                            ),
                            dcc.Interval(
                                id='interval-component-arch',
                                interval=20 * 1000,  # in milliseconds
                                n_intervals=0
                            ),

                        ], className='text-center architecture-radio-form'
                    ),

                ],
                    className='analysis-arch-select')
            ],
                width=2, className='analysis-column-left'),

            # COLUMN HOLDING ARCHITECTURE INFORMATION
            dbc.Col([

                # ACCESS INFORMATION ROW
                dbc.Row([

                    # COLUMN WITH ACCESS GRAPH
                    dbc.Col([

                        html.Div([
                            dcc.Loading(children=[
                                dcc.Graph(id='access-graph'),
                            ],
                                type="default", className='loading-heatmap')
                        ],
                            className='analysis-heatmap-view', id='architecture-view-div')

                    ], width=9, className='analysis-heatmap-column-left'),

                    # COLUMN WITH ACCESS GRAPH INTERFACE
                    dbc.Col([

                        html.Div([

                            dbc.FormGroup(
                                [
                                    dbc.Label("Plot Metric", className='analysis-sats-label label-margin-top'),
                                    dcc.Dropdown(
                                        options=[
                                            {'label': 'Access Time (avg)', 'value': 'ATavg'},
                                            {'label': 'Access Time (min)', 'value': 'ATmin'},
                                            {'label': 'Access Time (max)', 'value': 'ATmax'},
                                            {'label': 'Revisit Time (avg)', 'value': 'RvTavg'},
                                            {'label': 'Revisit Time (min)', 'value': 'RvTmin'},
                                            {'label': 'Revisit Time (max)', 'value': 'RvTmax'},
                                            {'label': 'Time To Coverage', 'value': 'TCcov'},
                                            {'label': 'Number of Pases', 'value': 'numPass'},
                                        ],
                                        value='ATavg',
                                        id='map-metric',
                                        multi=False,
                                        clearable=False,
                                        placeholder="choose local metric...",
                                        className='analysis-sats-label-dropdown',
                                    ),

                                ],
                                row=False,
                                className='specify-axis-format text-left'
                            ),

                            dbc.FormGroup(
                                [
                                    dbc.Label("Map Style", className='analysis-sats-label label-margin'),
                                    dcc.Dropdown(
                                        options=[
                                            {'label': 'Light', 'value': 'light'},
                                            {'label': 'Dark', 'value': 'dark'},
                                            {'label': 'Satellite', 'value': 'satellite'},
                                        ],
                                        value='satellite',
                                        id='map-style',
                                        multi=False,
                                        clearable=False,
                                        placeholder="choose local metric...",
                                        className='analysis-sats-label-dropdown',
                                    ),

                                ],
                                row=False,
                                className='specify-axis-format text-left'
                            ),

                            dbc.FormGroup(
                                [
                                    dbc.Label("Point Color", className='analysis-sats-label label-margin'),
                                    dcc.Dropdown(
                                        options=[
                                            {'label': 'Greyscale', 'value': 'Greys'},
                                            {'label': 'Blue-Red', 'value': 'Bluered'},
                                            {'label': 'Jet', 'value': 'Jet'},
                                            {'label': 'Viridis', 'value': 'Viridis'},
                                            {'label': 'Picnic', 'value': 'Picnic'},
                                            {'label': 'Redscale', 'value': 'Reds'},
                                            {'label': 'Bluescale', 'value': 'Blues'},
                                            {'label': 'Yellow-Green-Blue', 'value': 'YlGnBu'},
                                        ],
                                        value='Bluered',
                                        id='map-color',
                                        multi=False,
                                        clearable=False,
                                        placeholder="choose local metric...",
                                        className='analysis-sats-label-dropdown',
                                    ),

                                ],
                                row=False,
                                className='specify-axis-format text-left'
                            ),

                            dbc.FormGroup(
                                [
                                    dbc.Label("Point Size", className='analysis-sats-label label-margin'),
                                    dcc.Slider(
                                        value=5,
                                        id='map-marker-size',
                                        min=1,
                                        max=30,
                                        step=0.1,
                                    ),

                                ],
                                row=False,
                                className='specify-axis-format text-left'
                            ),

                            dbc.FormGroup(
                                [
                                    dbc.Label("Point Opacity", className='analysis-sats-label label-margin'),
                                    dcc.Slider(
                                        value=1,
                                        id='map-marker-opacity',
                                        min=0,
                                        max=1,
                                        step=0.01,
                                    ),

                                ],
                                row=False,
                                className='specify-axis-format text-left'
                            ),

                            dbc.FormGroup(
                                [
                                    dbc.Button("Reset Plot", color="primary", className="mr-1", id='reset-plot-button',
                                               style={'width': '100%'}),

                                ],
                                row=False,
                                className='specify-axis-format text-left'
                            ),

                        ],
                            className='analysis-heatmap-interface text-center', id='architecture-view-div')  ###

                    ], width=3, className='analysis-heatmap-column-right'),

                ], no_gutters=True, className='analysis-heatmap-row'),

                # Satellite information row
                dbc.Row([

                    dbc.Col([
                        html.Div([
                            html.Div([
                                dbc.FormGroup([
                                    dbc.Label("Satellites", className='analysis-sats-label'),
                                    dcc.Loading(children=[
                                        dbc.RadioItems(
                                            options=[],
                                            value='sat-0',
                                            id='analysis-sat-radio',
                                            className='analysis-sat-radio-class',
                                            labelClassName='analysis-sat-radio-label',
                                        ),
                                    ],
                                        type="circle")
                                ], className='text-center architecture-radio-form'),
                            ], className='analysis-sats-left-inner-div')
                        ], className='analysis-sats-left-div')
                    ], width=3, className='flex-column', ),

                    dbc.Col([
                        html.Div([
                            dcc.Tabs([
                                dcc.Tab(label="Specifications", value="sat_info1", style=analysis_tab_style,
                                        selected_style=analysis_tab_style),
                                dcc.Tab(label="Orbit", value="sat_info2", style=analysis_tab_style,
                                        selected_style=analysis_tab_style),
                                dcc.Tab(label="Instrument", value="sat_info3", style=analysis_tab_style,
                                        selected_style=analysis_tab_style),
                            ], id="sat-info-tabs", className='analysis-sat-tabs', value='sat_info1'),
                            html.Div([], id='analysis-sat-info-display', className='analysis-sat-info-display-class'),

                        ], className='analysis-sats-right-div', id='analysis-sats-information')
                    ], width=9, className='flex-column', ),

                ], no_gutters=True, className='analysis-architecture-details-row flex-row')

            ],
                width=10, className='analysis-column-right')

        ],
            no_gutters=False, className='analysis-row', )
    ],
        fluid=True, className='outter-container', )
],
    className='', )


# ------------------- CALLBACKS ------------------- #
@app.callback(
    Output('architecture-radio', 'options'),
    [
        Input('interval-component-arch', 'n_intervals'),
    ]
)
def update_architecture_list(n_intervals):
    ti.update_interfaces()
    options = []
    for arch in ti.arch_interfaces:
        options.append({'label': arch.get_name(), 'value': arch.get_name()})
    return options


@app.callback(
    [Output('access-graph', 'figure'), Output('analysis-sat-radio', 'options')],
    [
        Input('architecture-radio', 'value'),
        Input('map-metric', 'value'),
        Input('map-style', 'value'),
        Input('map-color', 'value'),
        Input('map-marker-size', 'value'),
        Input('map-marker-opacity', 'value'),
        Input('reset-plot-button', 'n_clicks')
    ],
    [State('access-graph', 'relayoutData')]
)
def render_architecture_analysis(arch, metric_type, map_type, map_color, map_marker_size, map_marker_opacity, n_clicks, relayout_data):
    return render_pointmap_graph(arch, metric_type, map_type, map_color, map_marker_size, map_marker_opacity, n_clicks, relayout_data), get_satellite_radio_options(arch)


def get_satellite_radio_options(arch):
    if (ti.get_arch_satellite_info(arch) is None):
        return []
    sat_list = ti.get_arch_satellite_info(arch)
    sat_ids = []
    for sat in sat_list:
        sat_id = str(sat['@id'])
        sat_ids.append({'label': sat_id, 'value': sat_id})
    return sat_ids


def render_card_details(ts_interface, arch):
    if (ts_interface.get_arch_satellite_info(arch) is None):
        return []

    sat_list = ts_interface.get_arch_satellite_info(arch)
    satellite_information = []
    for sat in sat_list:
        sat_id = str(sat['@id'])

        sat_volume = html.Div('Volume: ' + str(sat['volume']), className="card-text")
        sat_mass = html.Div('Mass: ' + str(sat['mass']), className="card-text")
        sat_power = html.Div('Power: ' + str(sat['power']), className="card-text")

        sat_sma = html.Div('Semimajor Axis: ' + str(sat['orbit']['semimajorAxis']), className="card-text")
        sat_inc = html.Div('Inclination: ' + str(round(sat['orbit']['inclination'], 2)), className="card-text")
        sat_ecc = html.Div('Eccentricity: ' + str(sat['orbit']['eccentricity']), className="card-text")
        sat_pa = html.Div('Periapsis Argument: ' + str(round(sat['orbit']['periapsisArgument'], 2)),
                          className="card-text")
        sat_raan = html.Div('RAAN: ' + str(round(sat['orbit']['rightAscensionAscendingNode'], 2)),
                            className="card-text")
        sat_ta = html.Div('True Anomaly: ' + str(round(sat['orbit']['trueAnomaly'], 2)), className="card-text")

        sat_info = dbc.Card([
            dbc.CardHeader("Satellite", className='interface-font-sm'),
            dbc.CardBody(
                [
                    html.H5(sat_id, className="card-title"),
                    sat_sma,
                    sat_inc,
                    sat_ecc,
                    sat_pa,
                    sat_raan,
                    sat_ta,
                ],
                id='orbital-info-card',
            )
        ],
            className='w-25 satellite-card-class',
        )
        satellite_information.append(sat_info)

    return satellite_information


def keys_exists(element, *keys):
    '''
    Check if *keys (nested) exists in `element` (dict).
    '''
    if not isinstance(element, dict):
        raise AttributeError('keys_exists() expects dict as first argument.')
    if len(keys) == 0:
        raise AttributeError('keys_exists() expects at least two arguments, one given.')

    _element = element
    for key in keys:
        try:
            _element = _element[key]
        except KeyError:
            return False
    return True


map_settings = {
    'bearing': 0,
    'center_lat': 0,
    'center_lon': 0,
    'pitch': 0,
    'zoom': 0,
    'first_calc': True,
}

reset_map_settings = {
    'bearing': 0,
    'center_lat': 0,
    'center_lon': 0,
    'pitch': 0,
    'zoom': 0,
}

plot_resets = -1


# in marker --> cmin / cmax are used to set the max values for the color
def render_pointmap_graph(value, metric_type, map_type, map_color, map_marker_size, map_marker_opacity, n_clicks, relayout_data):
    if (ti.get_arch_heatmap_data(value, metric_type) is None):
        return {'data': []}

    latitudes, longitudes, point_data, c_min, c_max = ti.get_arch_heatmap_data(value, metric_type)

    # {'mapbox.center': {'lon': -117.03172087917034, 'lat': 28.255889561560565}, 'mapbox.zoom': 4.5667132201213665,
    #  'mapbox.bearing': 0, 'mapbox.pitch': 0}
    print("CLICKS", n_clicks)


    min_lon = min(longitudes)
    max_lon = max(longitudes)
    lon_distance = abs(min_lon - max_lon)
    print("Lon distance", lon_distance)
    zoomies = 1
    center_lat = latitudes[int(len(latitudes) / 2)]
    center_lon = longitudes[int(len(longitudes) / 2)]


    global plot_resets
    if map_settings['first_calc'] is True:
        map_settings['center_lat'] = center_lat
        map_settings['center_lon'] = center_lon
        map_settings['zoom'] = zoomies
        map_settings['pitch'] = 0
        map_settings['bearing'] = 0
        map_settings['first_calc'] = False

        reset_map_settings['center_lat'] = center_lat
        reset_map_settings['center_lon'] = center_lon
        reset_map_settings['zoom'] = zoomies
        reset_map_settings['pitch'] = 0
        reset_map_settings['bearing'] = 0
        plot_resets = n_clicks
    else:
        if keys_exists(relayout_data, 'mapbox.center', 'lat'):
            map_settings['center_lat'] = relayout_data['mapbox.center']['lat']
        if keys_exists(relayout_data, 'mapbox.center', 'lon'):
            map_settings['center_lon'] = relayout_data['mapbox.center']['lon']
        if keys_exists(relayout_data, 'mapbox.zoom'):
            map_settings['zoom'] = relayout_data['mapbox.zoom']
        if keys_exists(relayout_data, 'mapbox.bearing'):
            map_settings['bearing'] = relayout_data['mapbox.bearing']
        if keys_exists(relayout_data, 'mapbox.pitch'):
            map_settings['pitch'] = relayout_data['mapbox.pitch']
        if plot_resets != n_clicks:
            plot_resets = n_clicks
            map_settings['center_lat'] = reset_map_settings['center_lat']
            map_settings['center_lon'] = reset_map_settings['center_lon']
            map_settings['zoom'] = reset_map_settings['zoom']
            map_settings['bearing'] = reset_map_settings['bearing']
            map_settings['pitch'] = reset_map_settings['pitch']



    return {
        'data': [
            go.Scattermapbox(
                lat=latitudes,
                lon=longitudes,
                mode='markers',
                marker=dict(
                    size=map_marker_size,
                    opacity=map_marker_opacity,
                    color=point_data,
                    cmin=c_min,
                    cmax=c_max,
                    colorscale=map_color,
                    showscale=True,
                    colorbar=dict(title=metric_type),
                ),
                text=point_data,
            )
        ],
        'layout': go.Layout(
            autosize=True,
            hovermode='closest',
            margin=go.layout.Margin(l=0, r=0, t=0, b=0),
            mapbox=go.layout.Mapbox(
                accesstoken=mapbox_access_token,
                bearing=map_settings['bearing'],
                style=map_type,
                center=go.layout.mapbox.Center(
                    lat=map_settings['center_lat'],
                    lon=map_settings['center_lon'],
                ),
                pitch=map_settings['pitch'],
                zoom=map_settings['zoom'],
            ),
        )

    }


@app.callback(
    Output('analysis-sat-info-display', 'children'),
    [
        Input('analysis-sat-radio', 'value'),
        Input('architecture-radio', 'value'),
        Input('sat-info-tabs', 'value'),
    ])
def get_satellite_information(sat, arch, category):
    # ts_interface = TradespaceInterface('/mission')
    sat = ti.get_satellite_by_id(arch, sat)
    if (sat == None):
        return []

    if (category == 'sat_info1'):
        return get_satellite_information_1(sat)
    elif (category == 'sat_info2'):
        return get_satellite_information_2(sat)
    elif (category == 'sat_info3'):
        return get_satellite_information_3(sat)

    return []


def get_satellite_information_1(sat):  # Satellite Specs
    sat_id = html.Div('ID: ' + str(sat['@id']))
    sat_name = html.Div('Name: ' + str(sat['name']))
    sat_mass = html.Div('Mass: ' + str(sat['mass']))
    sat_volume = html.Div('Volume: ' + str(sat['volume']))
    sat_power = html.Div('Power: ' + str(sat['power']))
    sat_tech = html.Div('Tech Readiness Level: ' + str(sat['techReadinessLevel']))
    sat_isGroundComm = html.Div('Is Ground Comm: ' + str(sat['isGroundCommand']))
    sat_isSpare = html.Div('Is Spare: ' + str(sat['isSpare']))
    sat_propellantType = html.Div('Propellant Type: ' + str(sat['propellantType']))
    sat_stabilizationType = html.Div('Stabilization Type: ' + str(sat['stabilizationType']))
    return [sat_id, sat_name, sat_volume, sat_mass, sat_power, sat_tech, sat_isGroundComm, sat_isSpare,
            sat_propellantType, sat_stabilizationType]


def get_satellite_information_2(sat):  # Orbital Specs
    orb = sat['orbit']
    sat_type = html.Div('Orbit Type: ' + str(orb['orbitType']))
    sat_SMA = html.Div('Semimajor Axis: ' + str(orb['semimajorAxis']))
    sat_INC = html.Div('Inclination: ' + str(orb['inclination']))
    sat_ECC = html.Div('Eccentricity: ' + str(orb['eccentricity']))
    sat_PA = html.Div('Periapsis Argument: ' + str(orb['periapsisArgument']))
    sat_RAAN = html.Div('Right Ascension of the Ascending Node: ' + str(orb['rightAscensionAscendingNode']))
    sat_TA = html.Div('True Anomaly: ' + str(orb['trueAnomaly']))
    sat_epoch = html.Div('Epoch: ' + str(orb['epoch']))
    return [sat_type, sat_SMA, sat_INC, sat_ECC, sat_PA, sat_RAAN, sat_TA, sat_epoch]


def get_satellite_information_3(sat):  # Instrument Specs
    pay = sat['payload'][0]
    fov = pay['fieldOfView']
    inst_geo = html.Div('Sensor Geometry: ' + str(fov['sensorGeometry']))
    inst_CT = html.Div('Cross Track FOV (full): ' + str(fov['crossTrackFieldOfView']))
    inst_AT = html.Div('Along Track FOV (full): ' + str(fov['alongTrackFieldOfView']))
    return [inst_geo, inst_CT, inst_AT]

# ------------------- CALLBACKS ------------------- #
